package application.manufacturingPlanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class FactoryInputsModel
{
	private final ObservableList<FactoryInput> factoryInputs = FXCollections.observableArrayList();
	public final ObservableList<FactoryInput> readOnlyFactoryInputs = FXCollections.unmodifiableObservableList(factoryInputs);

	public FactoryInputsModel(FactoryProductionStepsModel productionStepsModel)
	{
		productionStepsModel.rootModelNode.addEventHandler(TreeItem.treeNotificationEvent(), event -> {
			reCalculateFactoryInputs(productionStepsModel.rootModelNode);
		});
	}

	public void reCalculateFactoryInputs(TreeItem<FactoryProductionStep> rootIntermediary)
	{
		factoryInputs.clear();
		recursivelyAccumulateInputs(rootIntermediary);
	}

	private void recursivelyAccumulateInputs(TreeItem<FactoryProductionStep> productionStep)
	{
		if (productionStep.getChildren().isEmpty()) {
			FactoryProductionStep step = productionStep.getValue();

			boolean inputAlreadyExisted = false;
			for (FactoryInput input : factoryInputs) {
				if (input.getItem().equals(step.getItemProduced())) {
					input.itemsPerSecond.set(input.itemsPerSecond.get() + step.getItemProductionPerSecond());
					inputAlreadyExisted = true;
					break;
				}
			}

			if (!inputAlreadyExisted) {
				factoryInputs.add(new FactoryInput(step.getItemProduced(), step.getItemProductionPerSecond()));
			}
		} else {
			productionStep.getChildren().forEach(child -> {
				recursivelyAccumulateInputs(child);
			});
		}
	}
}
