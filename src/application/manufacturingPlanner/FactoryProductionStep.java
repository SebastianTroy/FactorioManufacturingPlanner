package application.manufacturingPlanner;

import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;

/**
 * This class exists to represent a process which has to occur within a factory to get a desired product. If a recipe produces multiple
 * items which are required for a factory, each requirement will have its own {@link FactoryProductionStep}.
 */
public class FactoryProductionStep
{
	private final Item itemProduced;
	public final SimpleDoubleProperty itemProductionPerSecond;
	private final ArrayList<Recipe> potentialRecipes;

	private final RecipesDatabase allRecipes;
	private final Collection<Item> preProducedItems;
	private final boolean usingExpensiveRecipes;

	private final SimpleObjectProperty<Recipe> selectedRecipe = new SimpleObjectProperty<Recipe>();
	protected final TreeItem<FactoryProductionStep> node = new TreeItem<FactoryProductionStep>(this);

	/**
	 * 
	 * @param item - The item this intermediary is producing
	 * @param requiredProductionPerSecond - The number of items per second this intermediary needs to products
	 * @param allRecipes - All recipes in the game
	 * @param preProducedItems - Optional items which will be provided, meaning they don't need producing, meaning they will be leaf nodes
	 *            like raw materials
	 * @param usingExpensiveRecipes - <code>true</code> if you're planning an expensive mode factory!
	 */
	public FactoryProductionStep(Item item, double requiredProductionPerSecond, RecipesDatabase allRecipes, Collection<Item> preProducedItems, boolean usingExpensiveRecipes)
	{
		this.itemProduced = item;
		this.itemProductionPerSecond = new SimpleDoubleProperty(requiredProductionPerSecond);
		this.potentialRecipes = allRecipes.getRecipesWhichProduce(item, usingExpensiveRecipes);

		this.allRecipes = allRecipes;
		this.preProducedItems = preProducedItems;
		this.usingExpensiveRecipes = usingExpensiveRecipes;

		node.setExpanded(true);

		if (!potentialRecipes.isEmpty()) {
			this.selectedRecipe.set(potentialRecipes.get(0));
			updateChildDependancies(this.selectedRecipe.get());
		}

		selectedRecipe.addListener((listener, oldRecipe, newRecipe) -> {
			updateChildDependancies(newRecipe);
		});
	}

	public void updateChildDependancies(Recipe selectedRecipe)
	{
		node.getChildren().clear();

		// Check we aren't on the list of pre-prepared items and that we aren't a raw material
		if (!preProducedItems.contains(itemProduced) && selectedRecipe != null) {
			// for each ingredient we require to be manufactured
			for (Item requiredIngredient : selectedRecipe.getIngredients(usingExpensiveRecipes).keySet()) {
				double ingredientCountPerItemProduced = selectedRecipe.getIngredients(usingExpensiveRecipes).get(requiredIngredient).doubleValue();
				FactoryProductionStep childProductionStep = new FactoryProductionStep(requiredIngredient, itemProductionPerSecond.doubleValue() * ingredientCountPerItemProduced, allRecipes, preProducedItems, usingExpensiveRecipes);
				node.getChildren().add(childProductionStep.node);
			}
		}
	}

	public Recipe getRecipe()
	{
		return selectedRecipe.get();
	}

	public Item getItemProduced()
	{
		return itemProduced;
	}

	public double getItemProductionPerSecond()
	{
		return itemProductionPerSecond.doubleValue();
	}

	@Override
	public String toString()
	{
		return itemProduced.name;
	}
}
