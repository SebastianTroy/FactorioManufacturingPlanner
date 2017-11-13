package application.manufacturingPlanner;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RecipesDatabase
{
	public ObservableList<Recipe> recipes = FXCollections.observableArrayList();

	public boolean contains(String recipeName)
	{
		for (Recipe recipe : recipes) {
			if (recipe.getName().equals(recipeName)) {
				return true;
			}
		}

		return false;
	}

	public Recipe getRecipeByName(String recipeName)
	{
		for (Recipe recipe : recipes) {
			if (recipe.getName().equals(recipeName)) {
				return recipe;
			}
		}

		return null;
	}

	public ArrayList<Recipe> getRecipesWhichProduce(Item itemProduced, boolean useExpensiveRecipes)
	{
		ArrayList<Recipe> toReturn = new ArrayList<Recipe>();
		recipes.forEach(recipe -> {
			if (recipe.getProducts(useExpensiveRecipes).containsKey(itemProduced)) {
				toReturn.add(recipe);
			}
		});
		return toReturn;
	}
}
