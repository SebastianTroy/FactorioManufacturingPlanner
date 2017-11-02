package application.gui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.json.simple.parser.ParseException;

import application.manufacturingPlanner.Recipe;
import application.recipeParser.RecipesParser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class MainWindow extends VBox
{
	@FXML
	TextField allRecipesFilter;
	@FXML
	ListView<Recipe> allRecipes;

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
		ArrayList<Recipe> recipes = new ArrayList<Recipe>();
		RecipesParser recipesParser = new RecipesParser();

		// For now, while we don't have lua to JSON file conversion going on, supply a JSON file instead of a lua one
		File recipiesDirectory = null;
		try {
			recipiesDirectory = new File(getClass().getResource("/recipes.json/../.").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if (recipiesDirectory.isDirectory()) {
			for (File recipesFile : recipiesDirectory.listFiles()) {
				if (recipesFile.isFile()) {
					try {
						recipes.addAll(recipesParser.parseRecipies(recipesFile));
					} catch (IOException | ParseException e) {
						System.out.println("File couldn't be parsed for recipes: " + recipesFile);
						e.printStackTrace();
					}
				}
			}
		}

		allRecipes.setItems(new SortedList<Recipe>(new FilteredList<Recipe>(FXCollections.observableArrayList(recipes), p -> true), (Recipe a, Recipe b) -> {
			return a.toString().compareTo(b.toString());
		}));

		allRecipesFilter.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if (((SortedList<Recipe>) allRecipes.getItems()).getSource() instanceof FilteredList<?>) {
					@SuppressWarnings("unchecked")
					FilteredList<Recipe> filteredList = (FilteredList<Recipe>) ((SortedList<Recipe>) allRecipes.getItems()).getSource();
					filteredList.setPredicate(new Predicate<Recipe>()
					{
						@Override
						public boolean test(Recipe recipe)
						{
							return recipe.toString().contains(newValue);
						}
					});
				}
			}
		});

		System.out.println("Num recipies: " + recipes.size());
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
