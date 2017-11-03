package application.manufacturingPlanner;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class FactoryProduct extends Recipe
{
	public enum ProductionRateUnit
	{
		Second, Minute, Hour,
	}

	private SimpleDoubleProperty productionRate = new SimpleDoubleProperty(1);
	private SimpleObjectProperty<ProductionRateUnit> productionRateUnit = new SimpleObjectProperty<ProductionRateUnit>(ProductionRateUnit.Minute);

	public FactoryProduct(Recipe other)
	{
		super(other);
	}

	public double getProductionRate()
	{
		return productionRate.get();
	}

	public double getProductionRatePerSecond()
	{
		double divisor = 0;

		switch (productionRateUnit.get()) {
			case Second:
				divisor = 1;
				break;
			case Minute:
				divisor = 60;
				break;
			case Hour:
				divisor = 60 * 60;
				break;
		}

		return productionRate.get() / divisor;
	}

	public ProductionRateUnit getProductionRateUnit()
	{
		return productionRateUnit.get();
	}

	public void setProductionRate(double newProductionRate)
	{
		productionRate.set(newProductionRate);
	}

	public void setProductionRateUnit(ProductionRateUnit newProductionRateUnit)
	{
		productionRateUnit.set(newProductionRateUnit);
	}
}
