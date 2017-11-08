package application.manufacturingPlanner;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class FactoryInput
{
	public SimpleObjectProperty<Item> item;
	public SimpleDoubleProperty itemsPerSecond;

	public FactoryInput(Item item, double itemsPerSecond)
	{
		this.item = new SimpleObjectProperty<Item>(item);
		this.itemsPerSecond = new SimpleDoubleProperty(itemsPerSecond);
	}

	public Item getItem()
	{
		return item.get();
	}

	public double getItemsPerSecond()
	{
		return itemsPerSecond.get();
	}
}
