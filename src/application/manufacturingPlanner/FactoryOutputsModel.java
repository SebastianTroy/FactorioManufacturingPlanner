package application.manufacturingPlanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Factory requirements are calculated backwards, working from what it will produce back to what it requires as an input.
 * 
 * this model is the first stage in that calculation, allowing the outputs to be set and notifying of any changes.
 *
 * this model contains {@link FactoryOutput}'s which are essentially {@link Item}s with some associated extra data, as such outputs are
 * added and removed in terms of {@link Item}s.
 */
public class FactoryOutputsModel
{
	private final ObservableList<FactoryOutput> factoryOutputs = FXCollections.observableArrayList();
	private final ObservableList<FactoryOutput> readOnlyFactoryOutputs = FXCollections.unmodifiableObservableList(factoryOutputs);

	// TODO don't expose the list, instead allow adding of listeners because changes to items in the list should also trigger updates, not just adding/subtracting
	public ObservableList<FactoryOutput> getfactoryOutputs()
	{
		// don't new the readOnly list here as the listeners would get cleaned up by the GC
		return readOnlyFactoryOutputs;
	}

	public boolean contains(Item itemtoCheckFor)
	{
		for (FactoryOutput factoryOutput : factoryOutputs) {
			if (factoryOutput.getName().equals(itemtoCheckFor.name)) {
				return true;
			}
		}

		return false;
	}

	// TODO allow adding of multiple items so updates are only triggered once per change, rather than per item (same for removing)
	public void addNewOutput(Item toAdd)
	{
		if (!contains(toAdd)) {
			factoryOutputs.add(new FactoryOutput(toAdd));
		}
	}

	public void removeOutput(Item toRemove)
	{
		factoryOutputs.removeIf(output -> {
			return output.item.equals(toRemove);
		});
	}
}
