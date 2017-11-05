package application.manufacturingPlanner;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class FactoryInput
{
	public SimpleStringProperty itemName;
	public SimpleDoubleProperty itemsPerSecond;
	
	public FactoryInput(String name, double itemsPerSecond)
	{
		this.itemName = new SimpleStringProperty(name);
		this.itemsPerSecond = new SimpleDoubleProperty(itemsPerSecond);
	}
	
	public String getItemName()
	{
		return itemName.get();
	}
	
	public double getItemsPerSecond()
	{
		return itemsPerSecond.get();
	}
}
