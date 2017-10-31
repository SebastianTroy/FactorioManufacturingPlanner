package application.manufacturingPlanner;

import java.util.Map;

public class Recipe
{
	// TODO enum to differentiate cheap and expensive recipes
	
	public String name;
	public Map<String, Number> ingredients;
	public Map<String, Number> products;
	
	public boolean equals(Recipe other) {
		return other.name.equals(this.name) && other.ingredients.equals(this.ingredients) && other.products.equals(this.products);
	}
}
