package application;

import java.io.IOException;

import application.gui.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PlannerApplication extends Application
{
	@Override
	public void start(Stage primaryStage)
	{
		try {
			initialiseGUI(primaryStage);
			primaryStage.show();

			// Do this after show because layouts don't work till then
			initialiseStage(primaryStage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		launch(args);
	}

	private final void initialiseStage(Stage primaryStage)
	{
		primaryStage.setTitle("Factorio Factory Planner");

		primaryStage.sizeToScene();
		primaryStage.setMinWidth(primaryStage.getWidth());
		primaryStage.setMinHeight(primaryStage.getHeight());
	}

	private final void initialiseGUI(Stage primaryStage)
	{
		// We'll load our GUI into this variable
		VBox guiRootNode = null;

		try {
			// Load the GUI
			FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource("MainWindow.fxml"));
			guiRootNode = (VBox) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// If we were not successful in loading our GUI
		if (guiRootNode == null) {
			new Alert(Alert.AlertType.ERROR, "The GUI could not be loaded, exiting application.").showAndWait();

			Platform.exit();
		} else {
			// Add our GUI to an unchanging Scene and add that to our stage
			primaryStage.setScene(new Scene(guiRootNode));
		}
	}

}
