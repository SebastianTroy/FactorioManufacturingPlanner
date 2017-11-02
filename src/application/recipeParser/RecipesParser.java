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
		Object categoryObject = recipeObject.get("category");

		// used for recipes with a normal & expensive variant
		Object normalDataObject = recipeObject.get("normal");
		Object expensiveDataObject = recipeObject.get("expensive");
		
		if (typeObject instanceof String && nameObject instanceof String) {
			if (typeObject.toString().equals("recipe")) {
				Recipe recipe = new Recipe();
				recipe.setName((String) nameObject);
				
				if (categoryObject instanceof String) {
					switch ((String) categoryObject) {
					case "crafting" : 
						/* fall-through */
					case "advanced-crafting" :
						recipe.setRequiredFacility(Recipe.Facility.Manufacturer);
						break;
					case "smelting" :
						recipe.setRequiredFacility(Recipe.Facility.Furnace);
						break;
					case "oil-processing" :
						recipe.setRequiredFacility(Recipe.Facility.Refinery);
						break;
					case "chemistry" :
						recipe.setRequiredFacility(Recipe.Facility.ChemicalPlant);
						break;
					case "crafting-with-fluid" :
						recipe.setRequiredFacility(Recipe.Facility.ManufacturerWithLiquid);
						break;
					case "centrifuging" :
						recipe.setRequiredFacility(Recipe.Facility.Centrifuge);
						break;
					case "rocket-building" :
						recipe.setRequiredFacility(Recipe.Facility.RocketSilo);
						break;
					}
				} else if (categoryObject == null) {
					// default to something sensible, I assume this is what the game logic does anyway
					recipe.setRequiredFacility(Recipe.Facility.Manufacturer);
				}
				
				if (normalDataObject instanceof JSONObject) {
					populateRecipeFromHashMap((JSONObject) normalDataObject, recipe, false);
				} else {
					populateRecipeFromHashMap(recipeObject, recipe, false);
				}
				
				if (expensiveDataObject instanceof JSONObject) {
					populateRecipeFromHashMap((JSONObject) expensiveDataObject, recipe, true);
				}
				
				if (recipe.isValid()) {
					parsedRecipies.add(recipe);
					return true;
				}
			}
		} 
		
		return false;
	}
	
	private void populateRecipeFromHashMap(JSONObject recipeComponents, Recipe toPopulate, boolean populateExpensiveComponents)
	{
		Object ingredientsObject = recipeComponents.get("ingredients");
		Object resultObject = recipeComponents.get("result");
		Object resultsObject = recipeComponents.get("results");
			
		if (ingredientsObject instanceof Map) {
				toPopulate.setIngredients(convertItemCountMapToHashMap((Map<?, ?>) ingredientsObject), populateExpensiveComponents);					
		}

		if (resultObject instanceof String) {
			HashMap<String, Number> products = new HashMap<String, Number>();
			products.put((String) resultObject, 1);
			toPopulate.setProducts(products, populateExpensiveComponents);
		} else if (resultsObject instanceof Map) {
			toPopulate.setProducts(convertItemCountMapToHashMap((Map<?, ?>) resultsObject), populateExpensiveComponents);
		} else if (resultsObject instanceof JSONArray) {
			toPopulate.setProducts(convertItemProbabilityArrayToHashMap((JSONArray) resultsObject), populateExpensiveComponents);
		}
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
			StringBuilder productNameBuilder = new StringBuilder();
			AtomicLong atomicProductProbability = new AtomicLong(1); // This defaults to one because in some cases probability isn't used and so this value wouldn't be modified
			AtomicLong atomicProductQuantity = new AtomicLong(0);
			if (productObject instanceof Map) {
				((Map<?, ?>) productObject).forEach((key, value) -> {
					if (key instanceof String && value instanceof Number) {
						if (key.equals("probability")) {
							atomicProductProbability.set(Double.doubleToLongBits(((Number)value).doubleValue()));
						} else {
							productNameBuilder.append((String) key);
							atomicProductQuantity.set(Double.doubleToLongBits(((Number)value).doubleValue()));
						}
					}
				});
			}
			
			String productName = productNameBuilder.toString();
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
}
