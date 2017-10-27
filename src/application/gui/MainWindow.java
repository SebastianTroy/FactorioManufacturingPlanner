package application.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

public class MainWindow extends VBox {
 	@FXML
	MenuItem defaultCss;
	@FXML
	MenuItem darkCss;

	@FXML
	ListView<String> list;
	
	public MainWindow() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
}

	@FXML
	private void onDefaultCssClicked() {
		getScene().getStylesheets().remove(getClass().getResource("gui/dark.css").toExternalForm());
		getScene().getStylesheets().add(getClass().getResource("gui/default.css").toExternalForm());
	}

	@FXML
	private void onDarkCssClicked() {
		getScene().getStylesheets().remove(getClass().getResource("gui/default.css").toExternalForm());
		getScene().getStylesheets().add(getClass().getResource("gui/dark.css").toExternalForm());
	}
}
