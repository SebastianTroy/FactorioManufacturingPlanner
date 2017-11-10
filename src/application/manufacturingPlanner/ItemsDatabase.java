package application.manufacturingPlanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ItemsDatabase
{
	public ObservableList<Item> items = FXCollections.observableArrayList();

	public boolean contains(Item item)
	{
		return contains(item.name);
	}
	
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
	
	/**
	 * Ensures only one instance of any given item type exists and that this
	 * database contains every instance of item.
	 * 
	 * Constructs a new {@link Item} if it isn't already in the database.
	 */
	@SuppressWarnings("deprecation")
	public Item getItem(String itemName) {
		if (!contains(itemName)) {
			items.add(new Item(itemName));
		}
		return getItemByName(itemName);
	}
}
