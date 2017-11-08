package application.manufacturingPlanner;

import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * This class exists to represent a process which has to occur within a factory to get a desired product. If a recipe produces multiple
 * items which are required for a factory, each requirement will have its own {@link FactoryProductionStep}.
 */
public class FactoryProductionStep
{
	private SimpleObjectProperty<Recipe> recipe = new SimpleObjectProperty<Recipe>();
	private SimpleObjectProperty<Item> itemProduced = new SimpleObjectProperty<Item>();
	private SimpleDoubleProperty itemProductionPerSecond = new SimpleDoubleProperty();
	private ArrayList<FactoryProductionStep> productionDependancies = new ArrayList<FactoryProductionStep>();

	/**
	 * 
	 * @param recipe - The recipe this intermediary represents
	 * @param item - The item this intermediary is producing
	 * @param requiredProductionPerSecond - The number of items per second this intermediary needs to products
	 * @param allRecipes - All recipes in the game
	 * @param usingExpensiveRecipes - <code>true</code> if you're planning an expensive mode factory!
	 */
	public FactoryProductionStep(Recipe recipe, Item item, double requiredProductionPerSecond, RecipesDatabase allRecipes, ItemsDatabase allItems, Collection<Item> itemsToNotManufacture, boolean usingExpensiveRecipes)
	{
		this.recipe.set(recipe);
		this.itemProduced.set(item);
		this.itemProductionPerSecond.set(requiredProductionPerSecond);

		// Check we aren't on the list of items to manufacture and that we have a recipe
		if (!itemsToNotManufacture.contains(item) && recipe != null) {
			// for each ingredient we require to be manufactured
			for (String requiredIngredientName : recipe.getIngredients(usingExpensiveRecipes).keySet()) {
				double ingredientItemsRequiredPerIntermediary = recipe.getIngredients(usingExpensiveRecipes).get(requiredIngredientName).doubleValue();
				ArrayList<Recipe> itemsWhichProduceRequiredIngredient = allRecipes.getRecipesWhichProduce(requiredIngredientName, usingExpensiveRecipes);
				for (Recipe rawRcipe : itemsWhichProduceRequiredIngredient) {
					// remove infinite recursion caused by the like of Korvax enrichment or coal liquefaction
					Recipe possibleIngredientProducer = rawRcipe.getRecipeNetIngredientsAndProducts();

					if (possibleIngredientProducer.getProducts(usingExpensiveRecipes).containsKey(requiredIngredientName)) {
						// work out how many items are needed per one of this produced
						productionDependancies.add(new FactoryProductionStep(possibleIngredientProducer, allItems.getItemByName(requiredIngredientName), this.itemProductionPerSecond.get() * ingredientItemsRequiredPerIntermediary, allRecipes, allItems, itemsToNotManufacture, usingExpensiveRecipes));
						// TODO support more than just the first recipe we stumble across (some things can be made in more than one way)
						break;
					}
				}
				if (itemsWhichProduceRequiredIngredient.isEmpty()) {
					productionDependancies.add(new FactoryProductionStep(null, allItems.getItemByName(requiredIngredientName), this.itemProductionPerSecond.get() * ingredientItemsRequiredPerIntermediary, allRecipes, allItems, itemsToNotManufacture, usingExpensiveRecipes));
				}
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
				if (input.item.get().equals(this.itemProduced.get())) {
					input.itemsPerSecond.set(input.itemsPerSecond.get() + this.itemProductionPerSecond.get());
					inputAlreadyExisted = true;
				}
			}

			if (!inputAlreadyExisted) {
				calculatedInputsDatabase.add(new FactoryInput(itemProduced.get(), itemProductionPerSecond.get()));
			}
		}
	}

	public Recipe getRecipe()
	{
		return recipe.get();
	}

	public Item getItemProduced()
	{
		return itemProduced.get();
	}

	public double getRequiredIntermediariesPerSecond()
	{
		return itemProductionPerSecond.get();
	}

	@Override
	public String toString()
	{
		return itemProduced.get().name;
	}
}
