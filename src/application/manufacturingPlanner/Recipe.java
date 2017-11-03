package application.manufacturingPlanner;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.beans.property.SimpleStringProperty;
import javafx.util.Pair;

public class Recipe
{
	public enum Facility
	{
		Unknown, Manufacturer, ManufacturerWithLiquid, Centrifuge, Refinery, ChemicalPlant, RocketSilo, Furnace,
	}

	private final SimpleStringProperty name = new SimpleStringProperty("");
	private HashMap<String, Number> normalIngredients = new HashMap<String, Number>();
	private HashMap<String, Number> normalProducts = new HashMap<String, Number>();
	private HashMap<String, Number> expensiveIngredients = new HashMap<String, Number>();
	private HashMap<String, Number> expensiveProducts = new HashMap<String, Number>();
	private Facility facilityRequired = Facility.Unknown;

	public Recipe()
	{
	}

	public Recipe(Recipe other)
	{
		setName(other.getName());
		setIngredients(other.normalIngredients, false);
		setIngredients(other.expensiveIngredients, true);
		setProducts(other.normalProducts, false);
		setProducts(other.expensiveProducts, true);
		setRequiredFacility(other.facilityRequired);
	}

	/**
	 * Some recipes have inputs and outputs of the same type, which messes with recursive calculations, so return the essence of the recipe
	 * (i.e. simplified from 5X in and 10X out, to just 5X out)
	 */
	public Recipe getRecipeNetIngredientsAndProducts()
	{
		Recipe netRecipe = new Recipe(this);
		
		java.util.function.BiConsumer<HashMap<String, Number>, HashMap<String, Number>> func = (ingredients, products) -> {
			// So we don't get concurrent exceptions, store the things to do then action them afterwards
			ArrayList<String> keysToRemove = new ArrayList<String>();
			ArrayList<Pair<String, Double>> netProductsToReAdd = new ArrayList<Pair<String, Double>>();
			
			// check the ingredients and products for matching items (i.e. same product for recipe used as ingredient)
			for (String ingredientKey : ingredients.keySet()) {
				if (products.containsKey(ingredientKey)) {
					double quantityConsumed = ingredients.get(ingredientKey).doubleValue();
					double quantityProduced = products.get(ingredientKey).doubleValue();
					Double netQuantityProduced = Double.valueOf(quantityProduced - quantityConsumed);
					
					keysToRemove.add(ingredientKey);
					netProductsToReAdd.add(new Pair<String, Double>(ingredientKey, netQuantityProduced));
				}
			}
			
			// now complete stored actions
			keysToRemove.forEach(key -> {
				ingredients.remove(key);
				products.remove(key);
			});
			netProductsToReAdd.forEach(pair -> {
				products.put(pair.getKey(), pair.getValue());
			});
		};
		
		func.accept(netRecipe.normalIngredients, netRecipe.normalProducts);
		func.accept(netRecipe.expensiveIngredients, netRecipe.expensiveProducts);
				
		return netRecipe;
	}

	public boolean equals(Recipe other)
	{
		return other.facilityRequired == this.facilityRequired && other.name.equals(this.name) && other.normalIngredients.equals(this.normalIngredients) && other.expensiveIngredients.equals(this.expensiveIngredients) && other.normalProducts.equals(this.normalProducts);
	}

	@Override
	public String toString()
	{
		return name.get();
	}

	public boolean isValid()
	{
		return facilityRequired != Facility.Unknown && !name.get().isEmpty() && !normalIngredients.isEmpty() && !normalProducts.isEmpty() && ((expensiveIngredients.isEmpty() && expensiveProducts.isEmpty()) || (!expensiveIngredients.isEmpty() && !expensiveProducts.isEmpty()));
	}

	public String getName()
	{
		return name.get();
	}

	public HashMap<String, Number> getIngredients(boolean returnExpensiveIngredientsIfPossible)
	{
		if (returnExpensiveIngredientsIfPossible && !expensiveIngredients.isEmpty()) {
			return expensiveIngredients;
		} else {
			return normalIngredients;
		}
	}

	public HashMap<String, Number> getProducts(boolean returnExpensiveProductsIfPossible)
	{
		if (returnExpensiveProductsIfPossible && !expensiveProducts.isEmpty()) {
			return expensiveProducts;
		} else {
			return normalProducts;
		}
	}

	public void setName(String name)
	{
		this.name.set(name);
	}

	public void setRequiredFacility(Facility facilityRequiredToProcessRecipe)
	{
		this.facilityRequired = facilityRequiredToProcessRecipe;
	}

	public void setIngredients(HashMap<String, Number> ingredients, boolean ingredientsAreExpensiveVareity)
	{
		if (ingredientsAreExpensiveVareity) {
			expensiveIngredients = ingredients;
		} else {
			normalIngredients = ingredients;
		}
	}

	public void setProducts(HashMap<String, Number> products, boolean productsAreExpensiveVareity)
	{
		if (productsAreExpensiveVareity) {
			expensiveProducts = products;
		} else {
			normalProducts = products;
		}
	}
}
