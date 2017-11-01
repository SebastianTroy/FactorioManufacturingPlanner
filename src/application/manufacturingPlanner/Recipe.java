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
	}
	
	public String name = "";
	public HashMap<String, Number> normalIngredients = new HashMap<String, Number>();
	public HashMap<String, Number> normalProducts = new HashMap<String, Number>();
	public HashMap<String, Number> expensiveIngredients = new HashMap<String, Number>();
	public HashMap<String, Number> expensiveProducts = new HashMap<String, Number>();
	public Facility facilityRequired = Facility.Unknown;
	
	@Override
	public String toString()
	{
		return name;
	}

	public boolean equals(Recipe other) {
		return other.facilityRequired == this.facilityRequired 
				&& other.name.equals(this.name) 
				&& other.normalIngredients.equals(this.normalIngredients) 
				&& other.expensiveIngredients.equals(this.expensiveIngredients) 
				&& other.normalProducts.equals(this.normalProducts);
	}
}
