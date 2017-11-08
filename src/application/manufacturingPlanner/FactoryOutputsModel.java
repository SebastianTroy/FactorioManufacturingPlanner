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
	private ObservableList<FactoryOutput> factoryOutputs = FXCollections.observableArrayList();
	private ObservableList<FactoryOutput> readOnlyFactoryOutputs = FXCollections.unmodifiableObservableList(factoryOutputs);

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

	public void addNewOutput(Item toAdd)
	{
		if (!contains(toAdd)) {
			factoryOutputs.add(new FactoryOutput(toAdd));
		}
	}

	public void removeOutput(Item toRemove)
	{
		factoryOutputs.remove(toRemove);
	}
}
