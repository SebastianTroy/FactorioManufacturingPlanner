package application.manufacturingPlanner;

public class Item {
	// public enum Type {
	// 	Solid,
	// 	Fluid
	// }
	
	public String name;
	// TODO type
	// TODO icon
	// TODO stack size
	
	public Item(String name)
	{
		this.name = name;
	}
	
	public boolean equals(Item other) 
	{
		return this.name.equals(other.name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
