package application.manufacturingPlanner;

import java.util.Map;

public class Recipie
{
	// TODO enum to differentiate cheap and expensive recipes
	
	public String name;
	public Map<String, Integer> ingredients;
	public Map<String, Integer> results;
	
	public boolean equals(Recipie other) {
		return other.name.equals(this.name) && other.ingredients.equals(this.ingredients) && other.results.equals(this.results);
	}
}
