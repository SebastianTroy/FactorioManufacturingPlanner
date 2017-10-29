package application.manufacturingPlanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class RecipiesParser
{
	public ArrayList<Recipie> parseRecipies(File recipiesFile) throws FileNotFoundException, IOException, ParseException
	{
		ArrayList<Recipie> parsedRecipies = new ArrayList<Recipie>();

		JSONArray recipiesJson = LuaToJsonConverter.getLuaRecipiesAsJson(recipiesFile);

		System.out.println(recipiesJson.getClass().getName());
		System.out.println("CONTENT types:");
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
					System.out.println(recipieObject);
				}
			} else {
				assert object instanceof JSONObject : "Skipped a recipie, was not of type JSONObject!";
			}
		}

		return parsedRecipies;
	}

	/**
	 * Checks the recipe object directly contains the "type", "name", "ingredients" & "result" items, and converts it to a {@link Recipie}
	 * then adds it to the parsedRecipies param.
	 * 
	 * This is the most common entry for Factorio version 0.15
	 * 
	 * @param recipeObject {@link JSONObject} to parse
	 * @param parsedRecipies The list of parsed recipes to add the parsed recipe to
	 * 
	 * @return <code>true</code> if the recipe was added to the list of parsed recipes, otherwise <code>false</code>.
	 */
	private boolean addBasicRecipie(JSONObject recipeObject, ArrayList<Recipie> parsedRecipies)
	{
		Object typeObject = recipeObject.get("types");
		Object nameObject = recipeObject.get("name");
		Object ingredientsObject = recipeObject.get("ingredients");
		Object resultObject = recipeObject.get("result");
		Object resultsObject = recipeObject.get("results");
		
		if (typeObject instanceof String && nameObject instanceof String && ingredientsObject instanceof HashMap) {
			if (typeObject.toString().equals("recipe")) {
				Recipie recipe = new Recipie();
				recipe.name = (String) nameObject;
				recipe.ingredients = convertJsonMapObjectToHashMap((Map<?, ?>) ingredientsObject);
				
				if (resultObject instanceof String) {
					// this assumes a single item of the result, recipes with multiple output will specify that
					recipe.results.put((String)resultObject, 1);
				} else if (resultsObject instanceof HashMap) {
					// this allows for multiple items to be produced, of more than one type
					recipe.results  = convertJsonMapObjectToHashMap((Map<?, ?>) resultsObject);
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
	private boolean addNormalAndExpensiveRecipe(JSONObject recipieObject, ArrayList<Recipie> parsedRecipies)
	{
		// TODO
		return false;
	}

	private HashMap<String, Integer> convertJsonMapObjectToHashMap(Map<?, ?> jsonMap)
	{
		HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
		jsonMap.forEach((key, value) -> {
			if (key instanceof String && value instanceof Integer) {
				hashMap.put((String)key, (Integer)value);
			} else {
				assert key instanceof String && value instanceof Integer : "Expected Map<String, Integer>";
			}
		});

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
