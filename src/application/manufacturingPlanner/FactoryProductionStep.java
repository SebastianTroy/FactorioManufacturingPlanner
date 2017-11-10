package application.manufacturingPlanner;

import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * This class exists to represent a process which has to occur within a factory to get a desired product. If a recipe produces multiple
 * items which are required for a factory, each requirement will have its own {@link FactoryProductionStep}.
 */
public class FactoryProductionStep
{
	private final Item itemProduced;
	private final double itemProductionPerSecond;
	private final ArrayList<Recipe> potentialRecipes;

	private final RecipesDatabase allRecipes;
	private final Collection<Item> preProducedItems;
	private final boolean usingExpensiveRecipes;
	
	private SimpleObjectProperty<Recipe> selectedRecipe = new SimpleObjectProperty<Recipe>();
	private ArrayList<FactoryProductionStep> productionDependancies = new ArrayList<FactoryProductionStep>();

	/**
	 * 
	 * @param item - The item this intermediary is producing
	 * @param requiredProductionPerSecond - The number of items per second this intermediary needs to products
	 * @param allRecipes - All recipes in the game
	 * @param preProducedItems - Optional items which will be provided, meaning they don't need producing, meaning they will be leaf nodes like raw materials
	 * @param usingExpensiveRecipes - <code>true</code> if you're planning an expensive mode factory!
	 */
	public FactoryProductionStep(Item item, double requiredProductionPerSecond, RecipesDatabase allRecipes, Collection<Item> preProducedItems, boolean usingExpensiveRecipes)
	{
		this.itemProduced = item;
		this.itemProductionPerSecond = requiredProductionPerSecond;
		this.potentialRecipes = allRecipes.getRecipesWhichProduce(item, usingExpensiveRecipes);
		
		this.allRecipes = allRecipes;
		this.preProducedItems = preProducedItems;
		this.usingExpensiveRecipes = usingExpensiveRecipes;

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
		productionDependancies.clear();

		// Check we aren't on the list of pre-prepared items and that we aren't a raw material
		if (!preProducedItems.contains(itemProduced) && selectedRecipe != null) {
			// for each ingredient we require to be manufactured
			for (Item requiredIngredient : selectedRecipe.getIngredients(usingExpensiveRecipes).keySet()) {
				double ingredientCountPerItemProduced = selectedRecipe.getIngredients(usingExpensiveRecipes).get(requiredIngredient).doubleValue();		
				productionDependancies.add(new FactoryProductionStep(requiredIngredient, itemProductionPerSecond * ingredientCountPerItemProduced, allRecipes, preProducedItems, usingExpensiveRecipes));
			}
		}
	}

	public void recursivelyBuildProductionDependancyTree(TreeItem<FactoryProductionStep> parentNode)
	{
		TreeItem<FactoryProductionStep> thisNode = new TreeItem<FactoryProductionStep>(this);
		productionDependancies.forEach(childNode -> {
			childNode.recursivelyBuildProductionDependancyTree(thisNode);
		});

		thisNode.setExpanded(true);
		parentNode.getChildren().add(thisNode);
	}

	public void recursivelyAccumulateFactoryInputs(ObservableList<FactoryInput> calculatedInputsDatabase)
	{
		if (!productionDependancies.isEmpty()) {
			productionDependancies.forEach(childNode -> {
				childNode.recursivelyAccumulateFactoryInputs(calculatedInputsDatabase);
			});
		} else {
			boolean inputAlreadyExisted = false;
			for (FactoryInput input : calculatedInputsDatabase) {
				if (input.item.get().equals(this.itemProduced)) {
					input.itemsPerSecond.set(input.itemsPerSecond.get() + itemProductionPerSecond);
					inputAlreadyExisted = true;
				}
			}

			if (!inputAlreadyExisted) {
				calculatedInputsDatabase.add(new FactoryInput(itemProduced, itemProductionPerSecond));
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

	public double getRequiredIntermediariesPerSecond()
	{
		return itemProductionPerSecond;
	}

	@Override
	public String toString()
	{
		return itemProduced.name;
	}
}
