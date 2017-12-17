package application.manufacturingPlanner;

import java.util.ArrayList;

import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class FactoryProductionStepsModel
{
	public final TreeItem<FactoryProductionStep> rootModelNode = new TreeItem<FactoryProductionStep>();

	// These items may change, which will in turn require a model update
	public final ObservableList<Item> optionalInputsDatabase = FXCollections.observableArrayList();
	public final ReadOnlyBooleanWrapper expensiveMode = new ReadOnlyBooleanWrapper(false);
	private final ObservableList<FactoryOutput> readOnlyFactoryOutputs;
	private final RecipesDatabase recipes;

	public FactoryProductionStepsModel(FactoryOutputsModel outputsModel, RecipesDatabase recipesDatabase)
	{
		rootModelNode.setExpanded(true);

		readOnlyFactoryOutputs = outputsModel.getfactoryOutputs();
		recipes = recipesDatabase;

		readOnlyFactoryOutputs.addListener((ListChangeListener<FactoryOutput>) change -> {
			updateProductionSteps();
		});
		optionalInputsDatabase.addListener((ListChangeListener<Item>) change -> {
			updateProductionSteps();
		});
		// FIXME model doesn't update when quantity or rate of an item is changed!
	}

	private void updateProductionSteps()
	{
		// collect the new production steps so we only have one tree change event at the end
		ArrayList<TreeItem<FactoryProductionStep>> newProductionSteps = new ArrayList<TreeItem<FactoryProductionStep>>();

		for (FactoryOutput product : readOnlyFactoryOutputs) {
			FactoryProductionStep productionStep = new FactoryProductionStep(product.item, product.getProductionRatePerSecond(), recipes, optionalInputsDatabase, expensiveMode.getValue());
			newProductionSteps.add(productionStep.node);
		}

		rootModelNode.getChildren().setAll(newProductionSteps);
	}
}
