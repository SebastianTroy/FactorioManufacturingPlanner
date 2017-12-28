package application.gui;

import application.manufacturingPlanner.Transport;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.text.TextAlignment;

/**
 * 
 *
 */
public class TransportComboBox extends ComboBox<Transport>
{
	public TransportComboBox(double itemCount) 
	{
		super(FXCollections.observableArrayList(new Transport(Transport.Type.BasicBelt, itemCount), new Transport(Transport.Type.FastBelt, itemCount), new Transport(Transport.Type.ExpressBelt, itemCount)));
		// TODO, render image of the colour of selected belt
		
		// TODO work out why the below is never called, despite the buttonCell being null at instantiation!
		buttonCellProperty().addListener((listener, oldValue, newValue) -> {
			if (newValue != null) {
				newValue.setTextAlignment(TextAlignment.RIGHT);
			}
		});

		getSelectionModel().clearAndSelect(0); // TODO select first with value < 1
	}
	
	public void setItemCount(Double itemCount)
	{
		getItems().forEach(item -> {
			item.setItemCount(itemCount);
		});
	}
}
