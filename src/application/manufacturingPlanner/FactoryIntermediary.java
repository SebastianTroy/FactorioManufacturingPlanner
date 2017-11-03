package application.manufacturingPlanner;

import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;

/**
 * This class exists to represent a process which has to occur within a factory to get the desired products.
 */
public class FactoryIntermediary
{
	private Recipe intermediaryRecipe;
	private SimpleStringProperty itemName = new SimpleStringProperty();
	private SimpleDoubleProperty requiredIntermediariesPerSecond = new SimpleDoubleProperty();
	private ArrayList<FactoryIntermediary> intermediaryDependancies = new ArrayList<FactoryIntermediary>();

	/**
	 * 
	 * @param recipe - The recipe this intermediary represents
	 * @param itemName - The item this intermediary is producing
	 * @param requiredProductionPerSecond - The number of items per second this intermediary needs to producs
	 * @param allRecipes - All recipes in the game
	 * @param usingExpensiveRecipes - <code>true</code> if you're planning an expensive mode factory!
	 */
	public FactoryIntermediary(Recipe recipe, String itemName, double requiredProductionPerSecond, Collection<Recipe> allRecipes, boolean usingExpensiveRecipes)
	{
		this.intermediaryRecipe = recipe;
		this.itemName.set(itemName);
		this.requiredIntermediariesPerSecond.set(requiredProductionPerSecond);

		// for each ingredient we require to be manufactured
		for (String requiredIngredient : intermediaryRecipe.getIngredients(usingExpensiveRecipes).keySet()) {
			for (Recipe rawRcipe : allRecipes) {
				// remove infinite recursion caused by the like of Korvax enrichment or coal liquefaction
				Recipe possibleIngredientProducer = rawRcipe.getRecipeNetIngredientsAndProducts();
				if (possibleIngredientProducer.getProducts(usingExpensiveRecipes).containsKey(requiredIngredient)) {
					// work out how many items are needed per one of this produced
					double ingredientItemsRequiredPerIntermediary = intermediaryRecipe.getIngredients(usingExpensiveRecipes).get(requiredIngredient).doubleValue();
					intermediaryDependancies.add(new FactoryIntermediary(possibleIngredientProducer, requiredIngredient, requiredIntermediariesPerSecond.get() * ingredientItemsRequiredPerIntermediary, allRecipes, usingExpensiveRecipes));
					// TODO support more than just the first recipe we stumble across (some things can be made in more than one way)
					break;
				}
			}
		}
	}

	public void recursivelyAddIntermediariesToTree(TreeItem<FactoryIntermediary> parentNode)
	{
		TreeItem<FactoryIntermediary> thisNode = new TreeItem<FactoryIntermediary>(this);

		intermediaryDependancies.forEach(childNode -> {
			childNode.recursivelyAddIntermediariesToTree(thisNode);
		});

		thisNode.setExpanded(true);
		parentNode.getChildren().add(thisNode);
	}

	public String getName()
	{
		return intermediaryRecipe.getName();
	}

	public String getItemName()
	{
		return itemName.get();
	}

	public double getRequiredIntermediariesPerSecond()
	{
		return requiredIntermediariesPerSecond.get();
	}
}
