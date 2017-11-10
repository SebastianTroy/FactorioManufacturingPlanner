package application;

import java.io.IOException;

import application.gui.MainWindowController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
		try {
			FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource("MainWindow.fxml"));
			loader.load();
			primaryStage.setScene(new Scene(loader.getRoot()));
		} catch (IOException e) {
			e.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "The GUI could not be loaded, exiting application.").showAndWait();
			Platform.exit();
		}
	}
}
