package application.gui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.json.simple.parser.ParseException;

import application.manufacturingPlanner.FactoryProduct;
import application.manufacturingPlanner.Recipe;
import application.recipeParser.RecipesParser;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;

public class MainWindow extends VBox
{
	@FXML
	TextField allRecipesFilter;
	@FXML
	ListView<Recipe> allRecipesList;
	@FXML
	Button addFactoryProduct;
	@FXML
	Button removeFactoryProduct;
	@FXML
	TableView<FactoryProduct> factoryProductsTable;
	@FXML
	TableColumn<FactoryProduct, String> factoryProductsTableProductNameColumn;
	@FXML
	TableColumn<FactoryProduct, Double> factoryProductsTableProductionRateColumn;
	@FXML
	TableColumn<FactoryProduct, FactoryProduct.ProductionRateUnit> factoryProductsTableProductionRateUnitColumn;

	@FXML
	MenuItem loadRecipiesButton;
	@FXML
	MenuItem defaultCssButton;
	@FXML
	MenuItem darkCssButton;

	private ObservableList<Recipe> allRecipesDatabase = FXCollections.observableArrayList();
	private ObservableList<FactoryProduct> factoryOutputsDatabase = FXCollections.observableArrayList();

	@FXML
	void initialize()
	{
		allRecipesList.setItems(new SortedList<Recipe>(new FilteredList<Recipe>(allRecipesDatabase, p -> true), (Recipe a, Recipe b) -> {
			return a.getName().compareTo(b.getName());
		}));

		allRecipesFilter.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				filterRecipesList();
			}
		});

		factoryOutputsDatabase.addListener((ListChangeListener.Change<? extends Recipe> c) -> {
			updateFactoryInputs();
		});

		factoryProductsTable.setItems(factoryOutputsDatabase);
		factoryProductsTableProductNameColumn.setCellValueFactory(new PropertyValueFactory<FactoryProduct, String>("name"));
		factoryProductsTableProductionRateColumn.setCellValueFactory(new PropertyValueFactory<FactoryProduct, Double>("productionRate"));
		factoryProductsTableProductionRateColumn.setCellFactory(TextFieldTableCell.<FactoryProduct, Double> forTableColumn(new DoubleStringConverter()));
		factoryProductsTableProductionRateColumn.setOnEditCommit(event -> {
			event.getRowValue().setProductionRate(event.getNewValue());
			updateFactoryInputs();
		});
		factoryProductsTableProductionRateUnitColumn.setCellValueFactory(new PropertyValueFactory<FactoryProduct, FactoryProduct.ProductionRateUnit>("productionRateUnit"));
		factoryProductsTableProductionRateUnitColumn.setCellFactory(ComboBoxTableCell.<FactoryProduct, FactoryProduct.ProductionRateUnit> forTableColumn(FactoryProduct.ProductionRateUnit.values()));
		factoryProductsTableProductionRateUnitColumn.setOnEditCommit(event -> {
			event.getRowValue().setProductionRateUnit(event.getNewValue());
			updateFactoryInputs();
		});
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

		allRecipesDatabase.setAll(recipes);

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

	@FXML
	private void onAddFactoryProductPressed()
	{
		ReadOnlyObjectProperty<Recipe> selectedRecipe = allRecipesList.getSelectionModel().selectedItemProperty();

		if (selectedRecipe.get() != null && !factoryOutputsDatabase.contains(selectedRecipe.get())) {
			factoryOutputsDatabase.add(new FactoryProduct(selectedRecipe.get()));
		}
	}

	@FXML
	private void onRemoveFactoryProductPressed()
	{
		ReadOnlyObjectProperty<FactoryProduct> selectedRecipe = factoryProductsTable.getSelectionModel().selectedItemProperty();

		if (selectedRecipe.get() != null) {
			factoryOutputsDatabase.remove(selectedRecipe.get());
		}
	}

	@FXML
	private void filterRecipesList()
	{
		if (((SortedList<Recipe>) allRecipesList.getItems()).getSource() instanceof FilteredList<?>) {
			@SuppressWarnings("unchecked")
			FilteredList<Recipe> filteredList = (FilteredList<Recipe>) ((SortedList<Recipe>) allRecipesList.getItems()).getSource();
			filteredList.setPredicate(new Predicate<Recipe>()
			{
				@Override
				public boolean test(Recipe recipe)
				{
					return recipe.getName().contains(allRecipesFilter.getText());
				}
			});
		}
	}
	
	private void updateFactoryInputs()
	{
		
	}
}
