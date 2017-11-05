package application.manufacturingPlanner;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class FactoryProduct
{
	public enum ProductionRateUnit
	{
		Second, Minute, Hour, CompressedYellowBelt, CompressedRedBelt, CompressedBlueBelt
	}

	private SimpleStringProperty productName = new SimpleStringProperty();
	private SimpleDoubleProperty productionRate = new SimpleDoubleProperty(1);
	private SimpleObjectProperty<ProductionRateUnit> productionRateUnit = new SimpleObjectProperty<ProductionRateUnit>(ProductionRateUnit.Minute);

	public FactoryProduct(String producName)
	{
		this.productName.set(producName);
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
				divisor = 1.0;
				break;
			case Minute:
				divisor = 60.0;
				break;
			case Hour:
				divisor = 60.0 * 60.0;
				break;
			case CompressedYellowBelt:
				divisor = 1.0 / 13.33;
				break;
			case CompressedRedBelt:
				divisor = 1.0 / 26.66;
				break;
			case CompressedBlueBelt:
				divisor = 1.0 / 40.0;
				break;
		}

		return productionRate.get() / divisor;
	}

	public String getProductName()
	{
		return productName.get();
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
