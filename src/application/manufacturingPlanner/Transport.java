package application.manufacturingPlanner;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Transport
{
	public enum TransportType
	{
		BasicBelt, FastBelt, ExpressBelt,
	}

	private final TransportType type;
	public final double capacity;
	public final boolean isFluid;

	public DoubleProperty itemCount = new SimpleDoubleProperty();

	public Transport(TransportType type)
	{
		this.type = type;
		switch (type) {
			case BasicBelt:
				this.capacity = 40.0 * (1.0 / 3.0);
				this.isFluid = false;
				break;
			case ExpressBelt:
				this.capacity = 40.0 * (2.0 / 3.0);
				this.isFluid = false;
				break;
			case FastBelt:
				this.capacity = 40.0 * (3.0 / 3.0);
				this.isFluid = false;
				break;
			default:
				this.capacity = 1;
				this.isFluid = false;
				break;
		}
	}
	
	public double getTransportCount()
	{
		return itemCount.doubleValue() / capacity;
	}

	@Override
	public String toString()
	{
		// TODO if fluid, we'd actually want to calculate the distance the quantity could flow without extra pumps required
		return type.toString() + ": " + String.format( "%.2f", getTransportCount());
	}
}
