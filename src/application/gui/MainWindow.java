package application.gui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import application.manufacturingPlanner.Recipie;
import application.manufacturingPlanner.RecipiesParser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class MainWindow extends VBox
{
	@FXML
	MenuItem loadRecipiesButton;
	@FXML
	MenuItem defaultCssButton;
	@FXML
	MenuItem darkCssButton;

	@FXML
	ListView<String> list;

	public MainWindow()
	{
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
	private void onLoadRecipiesButoonClicked()
	{
		FileChooser recipieFileChooser = new FileChooser();
		recipieFileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Factorio recipies.lua file", "*.lua"));

		// File recipiesFile = recipieFileChooser.showOpenDialog(this.getScene().getWindow());
		// For now, while we don't have lua to JSON file conversion going on, supply a JSON file instead of a lua one
		File recipiesFile = null;
		try {
			recipiesFile = new File(getClass().getResource("/recipe.json").toURI());
		} catch (URISyntaxException error) {
			error.printStackTrace();
		}

		if (recipiesFile != null) {
			RecipiesParser recipiesParser = new RecipiesParser();
			ArrayList<Recipie> recipies = null;
			try {
				recipies = recipiesParser.parseRecipies(recipiesFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (recipies != null) {
				System.out.println("Num recipies: " + recipies.size());
			}
		}
	}

	@FXML
	private void onDefaultCssClicked()
	{
		getScene().getStylesheets().remove(getClass().getResource("dark.css").toExternalForm());
		getScene().getStylesheets().add(getClass().getResource("default.css").toExternalForm());
	}

	@FXML
	private void onDarkCssClicked()
	{
		getScene().getStylesheets().remove(getClass().getResource("default.css").toExternalForm());
		getScene().getStylesheets().add(getClass().getResource("dark.css").toExternalForm());
	}
}
