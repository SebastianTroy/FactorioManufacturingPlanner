package application.manufacturingPlanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ItemsDatabase
{
	public ObservableList<Item> items = FXCollections.observableArrayList();

	public boolean contains(String itemName)
	{
		for (Item item : items) {
			if (item.name.equals(itemName)) {
				return true;
			}
		}

		return false;
	}

	public Item getItemByName(String itemName)
	{
		for (Item item : items) {
			if (item.name.equals(itemName)) {
				return item;
			}
		}

		return null;
	}
}
