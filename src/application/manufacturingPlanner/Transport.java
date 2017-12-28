package application.manufacturingPlanner;

public class Transport
{
	public enum Type {
		BasicBelt,
		FastBelt,
		ExpressBelt,
	}
	
	public final Type type;
	public double itemCount;
	
	public Transport(Type type, double itemCount)
	{
		this.type = type;
		this.itemCount = itemCount;
	}
	
	public void setItemCount(double itemCount)
	{
		this.itemCount = itemCount;
	}

	private double getCapacity()
	{
		switch(this.type) {
		case BasicBelt :
			return 40.0 * (1.0 / 3.0);
		case FastBelt :
			return 40.0 * (2.0 / 3.0);
		case ExpressBelt :
			return 40.0 * (3.0 / 3.0);
		}
		
		return 0;
	}
	
	@Override
	public String toString()
	{
		String retVal = new String();
		
		switch (this.type) {
		case BasicBelt:
			retVal += "Y";
			break;
		case FastBelt:
			retVal += "R";
			break;
		case ExpressBelt:
			retVal += "B";
			break;
		}
		
		return retVal + String.format("%.2f", itemCount / getCapacity());
	}
}
