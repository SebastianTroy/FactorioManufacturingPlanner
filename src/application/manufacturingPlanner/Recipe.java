package application.manufacturingPlanner;

import java.util.HashMap;

public class Recipe
{
	public enum Facility {
		Unknown,
		Manufacturer,
		ManufacturerWithLiquid,
		Centrifuge,
		Refinery,
		ChemicalPlant,
		RocketSilo, 
		Furnace,
	}
	
	private String name = "";
	private HashMap<String, Number> normalIngredients = new HashMap<String, Number>();
	private HashMap<String, Number> normalProducts = new HashMap<String, Number>();
	private HashMap<String, Number> expensiveIngredients = new HashMap<String, Number>();
	private HashMap<String, Number> expensiveProducts = new HashMap<String, Number>();
	private Facility facilityRequired = Facility.Unknown;
	
	public boolean equals(Recipe other) 
	{
		return other.facilityRequired == this.facilityRequired 
				&& other.name.equals(this.name) 
				&& other.normalIngredients.equals(this.normalIngredients) 
				&& other.expensiveIngredients.equals(this.expensiveIngredients) 
				&& other.normalProducts.equals(this.normalProducts);
	}

	@Override
	public String toString()
	{
		return name;
	}

	public boolean isValid() 
	{
		return  facilityRequired != Facility.Unknown
				&& !name.isEmpty()
				&& !normalIngredients.isEmpty()
				&& !normalProducts.isEmpty()
				&& ((expensiveIngredients.isEmpty() && expensiveProducts.isEmpty()) 
						|| (!expensiveIngredients.isEmpty() && !expensiveProducts.isEmpty()));
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
		this.name = name;
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
			normalProducts= products;
		}
	}
}
