package application.recipeParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import application.manufacturingPlanner.Recipe;

public class RecipesParser
{
	public ArrayList<Recipe> parseRecipies(File recipiesFile) throws FileNotFoundException, IOException, ParseException
	{
		ArrayList<Recipe> parsedRecipies = new ArrayList<Recipe>();

		JSONArray recipiesJson = LuaToJsonConverter.getLuaRecipiesAsJson(recipiesFile);

		for (Object object : recipiesJson) {
			if (object instanceof JSONObject) {
				JSONObject recipieObject = (JSONObject) object;

				// The format a recipe takes in the .lua is variable, try to interpret them in a way that doesn't break for new factorio
				// versions
				if (addBasicRecipie(recipieObject, parsedRecipies)) {
					// added a recipe
				} else if (addNormalAndExpensiveRecipe(recipieObject, parsedRecipies)) {
					// Added two recipes, one normal the other expensive
				} else {
					// A recipe wasn't recognised by any of the functions designed to handle them
					// If this is happening another function may need to be created to support the additional recipe format...
					// or the conversion from lua to JSON needs modifying
					System.out.println("FAILED: " + recipieObject);
				}
			} else {
				assert object instanceof JSONObject : "Skipped a recipie, was not of type JSONObject!";
			}
		}

		return parsedRecipies;
	}

	/**
	 * Checks the recipe object directly contains the "type", "name", "ingredients" & "result" items, and converts it to a {@link Recipe}
	 * then adds it to the parsedRecipies param.
	 * 
	 * This is the most common entry for Factorio version 0.15
	 * 
	 * @param recipeObject {@link JSONObject} to parse
	 * @param parsedRecipies The list of parsed recipes to add the parsed recipe to
	 * 
	 * @return <code>true</code> if the recipe was added to the list of parsed recipes, otherwise <code>false</code>.
	 */
	private boolean addBasicRecipie(JSONObject recipeObject, ArrayList<Recipe> parsedRecipies)
	{
		Object typeObject = recipeObject.get("types");
		Object nameObject = recipeObject.get("name");
		Object ingredientsObject = recipeObject.get("ingredients");
		Object resultObject = recipeObject.get("result");
		Object resultsObject = recipeObject.get("results");
		
		if (typeObject instanceof String && nameObject instanceof String && ingredientsObject instanceof HashMap) {
			if (typeObject.toString().equals("recipe")) {
				Recipe recipe = new Recipe();
				recipe.name = (String) nameObject;
				recipe.ingredients = convertItemCountMapToHashMap((Map<?, ?>) ingredientsObject);
				
				if (resultObject instanceof String) {
					// this assumes a single item of the result, recipes with multiple output will specify that
					recipe.products.put((String)resultObject, 1);
				} else if (resultsObject instanceof Map) {
					// this allows for multiple items to be produced, of more than one type
					recipe.products  = convertItemCountMapToHashMap((Map<?, ?>) resultsObject);
				} else if (resultsObject instanceof JSONArray) {
					// this allows for multiple items to be produced, of more than one type, where the actual quantity of items produced is non-integer due to a probabilistic output
					recipe.products  = convertItemProbabilityArrayToHashMap((JSONArray) resultsObject);
				} else {
					return false;
				}
				
				parsedRecipies.add(recipe);
			}
		} 
		
		return false;
	}

	/**
	 * TODO account for the fact that there are two recipies to add (one expensive, one cheap) 
	 * 
	 * @param recipieObject {@link JSONObject} to parse
	 * @param parsedRecipies The list of parsed recipes to add the parsed recipe to
	 * 
	 * @return <code>true</code> if the recipe was added to the list of parsed recipes, otherwise <code>false</code>.
	 */
	private boolean addNormalAndExpensiveRecipe(JSONObject recipieObject, ArrayList<Recipe> parsedRecipies)
	{
		// TODO
		return false;
	}

	private HashMap<String, Number> convertItemCountMapToHashMap(Map<?, ?> jsonMap)
	{
		HashMap<String, Number> hashMap = new HashMap<String, Number>();
		jsonMap.forEach((key, value) -> {
			if (key instanceof String && value instanceof Number) {
				hashMap.put((String)key, (Number)value);
			} else {
				assert key instanceof String && value instanceof Number : "Expected Map<String, Number>, got Map<" + key.getClass().getName() + ", " + value.getClass().getName() + ">";
			}
		});

		return hashMap;
	}
	
	private HashMap<String, Number> convertItemProbabilityArrayToHashMap(JSONArray jsonArray)
	{
		HashMap<String, Number> hashMap = new HashMap<String, Number>();
		
		for (Object productObject : jsonArray) {
			String productName = new String();
			AtomicLong atomicProductProbability = new AtomicLong(0);
			AtomicLong atomicProductQuantity = new AtomicLong(0);
			if (productObject instanceof Map) {
				((Map<?, ?>) productObject).forEach((key, value) -> {
					if (key instanceof String && value instanceof Number) {
						if (key.equals("probability")) {
							atomicProductProbability.set(Double.doubleToLongBits(((Number)value).doubleValue()));
						} else {
							productName.concat((String)key);
							atomicProductQuantity.set(Double.doubleToLongBits(((Number)value).doubleValue()));
						}
					}
				});
			}
			
			double productProbability = Double.longBitsToDouble(atomicProductProbability.longValue());
			double productQuantity = Double.longBitsToDouble(atomicProductQuantity.longValue());
			if (!productName.isEmpty() && productProbability * productQuantity != 0) {
				hashMap.put(productName, Double.valueOf(productProbability * productQuantity));
			} else {
				assert !productName.isEmpty() && productProbability * productQuantity != 0 : "Name[" + productName + "] Quantity[" + String.valueOf(productQuantity) + "] Probability[" + String.valueOf(productProbability) + "]";
			}
		}
		
		return hashMap;
	}

	/**
	 * The recipies in the .lua have a variable structure, for now just support the basic structure, but be aware that
	 * 
	 * <pre>
	 * private boolean checkKeysAreValid(JSONObject recipieObject)
	 * {
	 * 	if (recipieObject.containsKey("type") && recipieObject.containsKey("name")) {
	 * 		if (recipieObject.containsKey("ingredients") && recipieObject.containsKey("result")) {
	 * 			return true;
	 * 		} else if (recipieObject.containsKey("normal") && recipieObject.containsKey("expensive")) {
	 * 			JSONObject normalIngredientsObject = (JSONObject) recipieObject.get("normal");
	 * 			JSONObject expensiveInredientsObject = (JSONObject) recipieObject.get("expensive");
	 * 			if (normalIngredientsObject.containsKey("ingredients") && normalIngredientsObject.containsKey("result") && expensiveInredientsObject.containsKey("ingredients")
	 * 					&& expensiveInredientsObject.containsKey("result")) {
	 * 				return true;
	 * 			}
	 * 		}
	 * 	}
	 * 	System.out.println("Recipie contents not recognised");
	 * 	System.out.println(recipieObject);
	 * 	return false;
	 * }
	 * </pre>
	 */
}
