package application.gui;

import application.manufacturingPlanner.Transport;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

/**
 * 
 *
 */
public class TransportComboBox extends ComboBox<Transport>
{
	public TransportComboBox() 
	{
//		// TODO, render image of the colour of selected belt and display a value of the number of belts required for the itemCount
//		setCellFactory(value);Factory(callback -> {
//			return new ListCell<Transport>();
//		});
//		
//		get
		
		setItems(FXCollections.observableArrayList(Transport.BasicBelt, Transport.ExpressBelt, Transport.FastBelt));
	}
	
	public void setItemCount(Double itemCount)
	{
		// TODO check for null
		// TODO apply value to displayed value
	}
}
