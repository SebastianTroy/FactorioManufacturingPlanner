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
				if (!addRecipie(recipieObject, parsedRecipies)) {
					System.out.println("FAILED: " + recipieObject);
				}
			} else {
				assert object instanceof JSONObject : "Skipped a recipie, was not of type JSONObject!";
			}
		}

		return parsedRecipies;
	}

	/**
	 * Checks the recipe object for items like "type", "name", "ingredients" & "result" items, and converts it to a {@link Recipe}
	 * then adds it to the parsedRecipies param.
	 * 
	 * Mandatory items:
	 *  - "type"
	 *  - "name"
	 *  - One of:
	 *    - "result"
	 *    - "results"
	 *    - "normal & "expensive"
	 * 
	 * @param recipeObject {@link JSONObject} to parse
	 * @param parsedRecipies The list of parsed recipes to add the parsed recipe to
	 * 
	 * @return <code>true</code> if the recipe was added to the list of parsed recipes, otherwise <code>false</code>.
	 */
	private boolean addRecipie(JSONObject recipeObject, ArrayList<Recipe> parsedRecipies)
	{
		// used for all recipes
		Object typeObject = recipeObject.get("type");
		Object nameObject = recipeObject.get("name");		
		Object normalIngredientsObject = recipeObject.get("ingredients");
		Object normalResultObject = recipeObject.get("result");
		Object normalResultsObject = recipeObject.get("results");
		
		// used for recipes with a normal & expensive variant
		Object normalDataObject = recipeObject.get("normal");
		Object expensiveDataObject = recipeObject.get("expensive");
		Object expensiveIngredientsObject = null;
		Object expensiveResultObject = null;
		Object expensiveResultsObject = null;
		
		if (typeObject instanceof String && nameObject instanceof String) {
			if (typeObject.toString().equals("recipe")) {
				Recipe recipe = new Recipe();
				recipe.name = (String) nameObject;
				
				if (normalDataObject instanceof Map && expensiveDataObject instanceof Map) {
					// TODO parse these objects and populate both normal and expensive ingredients and results objects
					
					// ----- expensive -----
					if (expensiveIngredientsObject instanceof Map) {
						recipe.expensiveIngredients = convertItemCountMapToHashMap((Map<?, ?>) expensiveIngredientsObject);					
					}
					
					if (expensiveResultObject instanceof String) {
						// this assumes a single item of the result
						recipe.expensiveProducts.put((String) expensiveResultObject, 1);
					} else if (expensiveResultsObject instanceof Map) {
						// this allows for multiple items to be produced, of more than one type
						recipe.expensiveProducts = convertItemCountMapToHashMap((Map<?, ?>) expensiveResultsObject);
					} else if (expensiveResultsObject instanceof JSONArray) {
						// this allows for multiple items to be produced, of more than one type, where the actual quantity of items produced is non-integer due to a probabilistic output
						recipe.expensiveProducts = convertItemProbabilityArrayToHashMap((JSONArray) expensiveResultsObject);
					} else {
						return false;
					}
				}
				
				// TODO code duplication above & below, local lambda?
				
				// ----- normal -----
				if (normalIngredientsObject instanceof Map) {
					recipe.normalIngredients = convertItemCountMapToHashMap((Map<?, ?>) normalIngredientsObject);					
				}
				
				if (normalResultObject instanceof String) {
					// this assumes a single item of the result
					recipe.normalProducts.put((String) normalResultObject, 1);
				} else if (normalResultsObject instanceof Map) {
					// this allows for multiple items to be produced, of more than one type
					recipe.normalProducts = convertItemCountMapToHashMap((Map<?, ?>) normalResultsObject);
				} else if (normalResultsObject instanceof JSONArray) {
					// this allows for multiple items to be produced, of more than one type, where the actual quantity of items produced is non-integer due to a probabilistic output
					recipe.normalProducts = convertItemProbabilityArrayToHashMap((JSONArray) normalResultsObject);
				} else {
					return false;
				}
				
				parsedRecipies.add(recipe);
				return true;
			}
		} 
		
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
