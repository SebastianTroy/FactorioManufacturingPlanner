package application.manufacturingPlanner;

import java.util.HashMap;

public class Recipe
{
	// TODO enum to differentiate cheap and expensive recipes
	
	public String name;
	public HashMap<String, Number> ingredients = new HashMap<String, Number>();
	public HashMap<String, Number> products = new HashMap<String, Number>();;
	

	public boolean equals(Recipe other) {
		return other.name.equals(this.name) && other.ingredients.equals(this.ingredients) && other.products.equals(this.products);
	}
}
