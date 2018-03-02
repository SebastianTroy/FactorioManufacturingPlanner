package application.gui;

import application.manufacturingPlanner.Transport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

/**
 * 
 *
 */
public class TransportComboBox extends ComboBox<Transport>
{
	public TransportComboBox()
	{
		setEditable(false);
	}

	public void setItemCount(Double itemCount)
	{
		// TODO, render image of the colour of selected belt
		ObservableList<Transport> transportItems = FXCollections.observableArrayList(new Transport(Transport.TransportType.BasicBelt), new Transport(Transport.TransportType.ExpressBelt), new Transport(Transport.TransportType.FastBelt));

		if (itemCount != null) {
			transportItems.forEach(item -> {
				item.itemCount.set(itemCount.doubleValue());
			});
		} else {
			transportItems.forEach(item -> {
				item.itemCount.set(0.0);
			});
		}
		setItems(transportItems);
		getSelectionModel().selectFirst();
		if (getSelectionModel().getSelectedItem().getTransportCount() > 1.0) {
			getSelectionModel().selectNext();
		}
		if (getSelectionModel().getSelectedItem().getTransportCount() > 1.0) {
			getSelectionModel().selectNext();
		}
	}
}
