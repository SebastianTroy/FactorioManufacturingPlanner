package application.manufacturingPlanner;

public enum Transport
{
	BasicBelt(40.0 * (1.0 / 3.0), false), FastBelt(40.0 * (2.0 / 2.0), false), ExpressBelt(40.0 * (3.0 / 3.0), false);

	public final double capacity;
	public final boolean isFluid;

	private Transport(double capacity, boolean isFluid)
	{
		this.capacity = capacity;
		this.isFluid = isFluid;
	}
	
	// TODO this needs to be a class so we can specify a quantity upon instantiation, then we can list instances in combo-boxes and display them sensibly
}
