package application.gui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.json.simple.parser.ParseException;

import application.manufacturingPlanner.FactoryInput;
import application.manufacturingPlanner.FactoryIntermediary;
import application.manufacturingPlanner.FactoryOutput;
import application.manufacturingPlanner.Item;
import application.manufacturingPlanner.Recipe;
import application.recipeParser.RecipesParser;
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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;

public class MainWindow extends VBox
{
	// ----- menu bar -----
	@FXML
	MenuItem loadRecipiesButton;
	@FXML
	MenuItem defaultCssButton;
	@FXML
	MenuItem darkCssButton;

	// ----- factory setup -----
	@FXML
	TextField allProductsFilter;
	@FXML
	ListView<String> allProductsList;
	@FXML
	Button addFactoryProduct;
	@FXML
	Button removeFactoryProduct;
	@FXML
	TableView<FactoryOutput> factoryProductsTable;
	@FXML
	TableColumn<FactoryOutput, String> factoryProductsTableProductNameColumn;
	@FXML
	TableColumn<FactoryOutput, Double> factoryProductsTableProductionRateColumn;
	@FXML
	TableColumn<FactoryOutput, FactoryOutput.ProductionRateUnit> factoryProductsTableProductionRateUnitColumn;

	@FXML
	ListView<String> selectedOptionalInputsList;
	@FXML
	Button addOptionalFactoryInput;
	@FXML
	Button removeOptionalFactoryInput;

	
	// ----- factory details -----
	@FXML
	TreeTableView<FactoryIntermediary> factoryIntermediariesTable;
	@FXML
	TreeTableColumn<FactoryIntermediary, String> factoryIntermediariesTableIntermediaryName;
	@FXML
	TreeTableColumn<FactoryIntermediary, String> factoryIntermediariesTableIntermediaryItemName;
	@FXML
	TreeTableColumn<FactoryIntermediary, Double> factoryIntermediariesTableCountPerSecond;
	
	@FXML
	TableView<FactoryInput> factoryInputsTable;
	@FXML
	TableColumn<FactoryInput, String> factoryInputssTableItemNameColumn;
	@FXML
	TableColumn<FactoryInput, Double> factoryInputsTableInputRateColumn;

	private ObservableList<Recipe> allRecipesDatabase = FXCollections.observableArrayList();
	private ObservableList<String> allProductsDatabase = FXCollections.observableArrayList();
	private ObservableList<String> selectedOptionalInputsDatabase = FXCollections.observableArrayList();
	private ObservableList<FactoryOutput> selectedOutputsDatabase = FXCollections.observableArrayList();
	private ObservableList<FactoryIntermediary> calculatedIntermediariesDatabase = FXCollections.observableArrayList();
	private ObservableList<FactoryInput> calculatedInputsDatabase = FXCollections.observableArrayList();

	@FXML
	void initialize()
	{
		setTextEditToFilterListView(allProductsList, allProductsDatabase, allProductsFilter);
		setTextEditToFilterListView(allProductsList, allProductsDatabase, allProductsFilter);

		selectedOutputsDatabase.addListener((ListChangeListener.Change<? extends FactoryOutput> c) -> {
			updateFactory();
		});
		selectedOptionalInputsDatabase.addListener((ListChangeListener.Change<? extends String> c) -> {
			updateFactory();
		});
		
		allProductsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		factoryProductsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		selectedOptionalInputsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		factoryProductsTable.setItems(selectedOutputsDatabase);
		factoryProductsTableProductNameColumn.setCellValueFactory(new PropertyValueFactory<FactoryOutput, String>("name"));
		factoryProductsTableProductionRateColumn.setCellValueFactory(new PropertyValueFactory<FactoryOutput, Double>("productionRate"));
		factoryProductsTableProductionRateColumn.setCellFactory(TextFieldTableCell.<FactoryOutput, Double> forTableColumn(new DoubleStringConverter()));
		factoryProductsTableProductionRateColumn.setOnEditCommit(event -> {
			event.getRowValue().setProductionRate(event.getNewValue());
			updateFactory();
		});
		factoryProductsTableProductionRateUnitColumn.setCellValueFactory(new PropertyValueFactory<FactoryOutput, FactoryOutput.ProductionRateUnit>("productionRateUnit"));
		factoryProductsTableProductionRateUnitColumn.setCellFactory(ComboBoxTableCell.<FactoryOutput, FactoryOutput.ProductionRateUnit> forTableColumn(FactoryOutput.ProductionRateUnit.values()));
		factoryProductsTableProductionRateUnitColumn.setOnEditCommit(event -> {
			event.getRowValue().setProductionRateUnit(event.getNewValue());
			updateFactory();
		});
		
		selectedOptionalInputsList.setItems(selectedOptionalInputsDatabase);

		factoryIntermediariesTable.setShowRoot(false);
		factoryIntermediariesTableIntermediaryName.setCellValueFactory(new TreeItemPropertyValueFactory<FactoryIntermediary, String>("name"));
		factoryIntermediariesTableIntermediaryItemName.setCellValueFactory(new TreeItemPropertyValueFactory<FactoryIntermediary, String>("itemName"));
		factoryIntermediariesTableCountPerSecond.setCellValueFactory(new TreeItemPropertyValueFactory<FactoryIntermediary, Double>("requiredIntermediariesPerSecond"));
		
		factoryInputsTable.setItems(calculatedInputsDatabase);
		factoryInputssTableItemNameColumn.setCellValueFactory(new PropertyValueFactory<FactoryInput, String>("itemName"));
		factoryInputsTableInputRateColumn.setCellValueFactory(new PropertyValueFactory<FactoryInput, Double>("itemsPerSecond"));
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
		System.out.println("Num recipies: " + allRecipesDatabase.size());

		allRecipesDatabase.forEach(recipe -> {
			recipe.getProducts(true).forEach((itemName, itemCount) -> {
				if (!allProductsDatabase.contains(itemName)) {
					allProductsDatabase.add(itemName);
				}
			});
		});
		System.out.println("Num Item Types: " + allProductsDatabase.size());
	}

	@FXML
	private void onDefaultCssClicked()
	{
		// TODO getScene() returns null...
		
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
		allProductsList.getSelectionModel().getSelectedItems().forEach(item -> {
			selectedOutputsDatabase.add(new FactoryOutput(new Item(item)));
		});		
	}

	@FXML
	private void onRemoveFactoryProductPressed()
	{
		// create copy of list so that we don't try to iterate and remove from our selectionModel at the same time!
		new ArrayList<FactoryOutput>(factoryProductsTable.getSelectionModel().getSelectedItems()).forEach(item -> {
			selectedOutputsDatabase.remove(item);
		});		
	}
	
	@FXML
	private void onAddOptionalFactoryInputPressed()
	{
		allProductsList.getSelectionModel().getSelectedItems().forEach(item -> {
			selectedOptionalInputsDatabase.add(item);
		});		
	}

	@FXML
	private void onRemoveoptionalFactoryInputPressed()
	{
		// create copy of list so that we don't try to iterate and remove from our selectionModel at the same time!
		new ArrayList<String>(selectedOptionalInputsList.getSelectionModel().getSelectedItems()).forEach(item -> {
			selectedOptionalInputsDatabase.remove(item);
		});		
	}
	
	private void setTextEditToFilterListView(ListView<String> listToFilter, ObservableList<String> listItemsModel, TextField filterInput)
	{
		FilteredList<String> filteredFactoryProductsList = new FilteredList<String>(listItemsModel);
	
		listToFilter.setItems(new SortedList<String>(filteredFactoryProductsList, (String a, String b) -> {
			return a.compareTo(b);
		}));		
		
		filterInput.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				filterItemList(filteredFactoryProductsList, newValue);
			}
		});
	}

	private void filterItemList(FilteredList<String> toFilter, String filterText)
	{
			toFilter.setPredicate(new Predicate<String>()
			{
				@Override
				public boolean test(String productName)
				{
					return productName.contains(filterText);
				}
			});
	}
	
	private void updateFactory()
	{
		ArrayList<FactoryIntermediary> intermediaries = new ArrayList<FactoryIntermediary>();

		// TODO allow for expensive recipes
		boolean useExpensiveRecipes = false;
		
		for (FactoryOutput product : selectedOutputsDatabase) {
			for (Recipe recipe : allRecipesDatabase) {
				if (recipe.getProducts(useExpensiveRecipes).containsKey(product.getName())) {
					intermediaries.add(new FactoryIntermediary(recipe, product.getName(), product.getProductionRatePerSecond(), allRecipesDatabase, selectedOptionalInputsDatabase, useExpensiveRecipes));
					// TODO do better then just using the first recipe we find
					break;
				}
			}
		}

		calculatedIntermediariesDatabase.setAll(intermediaries);

		calculatedInputsDatabase.clear();
		TreeItem<FactoryIntermediary> rootIntermediary = new TreeItem<FactoryIntermediary>();
		rootIntermediary.setExpanded(true);
		for (FactoryIntermediary topLevelIntermediary : intermediaries) {
			topLevelIntermediary.recursivelyAddIntermediariesToTree(rootIntermediary);
			topLevelIntermediary.recursivelyAccumulateFactoryInputs(calculatedInputsDatabase);
		}
		factoryIntermediariesTable.setRoot(rootIntermediary);
	}
}
