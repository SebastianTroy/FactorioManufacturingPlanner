package application.gui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.json.simple.parser.ParseException;

import application.gameFileParser.RecipesParser;
import application.manufacturingPlanner.FactoryInput;
import application.manufacturingPlanner.FactoryOutput;
import application.manufacturingPlanner.FactoryProductionStep;
import application.manufacturingPlanner.Item;
import application.manufacturingPlanner.ItemsDatabase;
import application.manufacturingPlanner.Recipe;
import application.manufacturingPlanner.RecipesDatabase;
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
	TextField allItemsFilter;
	@FXML
	ListView<Item> allItemsList;
	@FXML
	Button addFactoryProduct;
	@FXML
	Button removeFactoryProduct;
	@FXML
	TableView<FactoryOutput> factoryOutputsTable;
	@FXML
	TableColumn<FactoryOutput, String> factoryOutputsTableProductNameColumn;
	@FXML
	TableColumn<FactoryOutput, Double> factoryOutputsTableProductionRateColumn;
	@FXML
	TableColumn<FactoryOutput, FactoryOutput.ProductionRateUnit> factoryOutputsTableProductionRateUnitColumn;

	@FXML
	ListView<Item> optionalInputItemsList;
	@FXML
	Button addOptionalItemInput;
	@FXML
	Button removeOptionalItemInput;

	// ----- factory details -----
	@FXML
	TreeTableView<FactoryProductionStep> factoryIntermediariesTable;
	@FXML
	TreeTableColumn<FactoryProductionStep, Recipe> factoryIntermediariesRecipe;
	@FXML
	TreeTableColumn<FactoryProductionStep, Item> factoryIntermediariesTableIntermediaryItem;
	@FXML
	TreeTableColumn<FactoryProductionStep, Double> factoryIntermediariesTableCountPerSecond;

	@FXML
	TableView<FactoryInput> factoryInputsTable;
	@FXML
	TableColumn<FactoryInput, String> factoryInputsTableItemNameColumn;
	@FXML
	TableColumn<FactoryInput, Double> factoryInputsTableInputRateColumn;

	private RecipesDatabase allRecipes = new RecipesDatabase();
	private ItemsDatabase allItems = new ItemsDatabase();
	private ObservableList<Item> optionalInputsDatabase = FXCollections.observableArrayList();
	private ObservableList<FactoryOutput> selectedOutputsDatabase = FXCollections.observableArrayList();
	private ObservableList<FactoryProductionStep> calculatedIntermediariesDatabase = FXCollections.observableArrayList();
	private ObservableList<FactoryInput> calculatedInputsDatabase = FXCollections.observableArrayList();

	@FXML
	void initialize()
	{
		setTextEditToFilterListView(allItemsList, allItems.items, allItemsFilter);

		selectedOutputsDatabase.addListener((ListChangeListener.Change<? extends FactoryOutput> c) -> {
			updateFactory();
		});
		optionalInputsDatabase.addListener((ListChangeListener.Change<? extends Item> c) -> {
			updateFactory();
		});

		allItemsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		factoryOutputsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		optionalInputItemsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		factoryOutputsTable.setItems(selectedOutputsDatabase);
		factoryOutputsTableProductNameColumn.setCellValueFactory(new PropertyValueFactory<FactoryOutput, String>("name"));
		factoryOutputsTableProductionRateColumn.setCellValueFactory(new PropertyValueFactory<FactoryOutput, Double>("productionRate"));
		factoryOutputsTableProductionRateColumn.setCellFactory(TextFieldTableCell.<FactoryOutput, Double> forTableColumn(new DoubleStringConverter()));
		factoryOutputsTableProductionRateColumn.setOnEditCommit(event -> {
			event.getRowValue().setProductionRate(event.getNewValue());
			updateFactory();
		});
		factoryOutputsTableProductionRateUnitColumn.setCellValueFactory(new PropertyValueFactory<FactoryOutput, FactoryOutput.ProductionRateUnit>("productionRateUnit"));
		factoryOutputsTableProductionRateUnitColumn.setCellFactory(ComboBoxTableCell.<FactoryOutput, FactoryOutput.ProductionRateUnit> forTableColumn(FactoryOutput.ProductionRateUnit.values()));
		factoryOutputsTableProductionRateUnitColumn.setOnEditCommit(event -> {
			event.getRowValue().setProductionRateUnit(event.getNewValue());
			updateFactory();
		});

		optionalInputItemsList.setItems(optionalInputsDatabase);

		factoryIntermediariesTable.setShowRoot(false);
		factoryIntermediariesRecipe.setCellValueFactory(new TreeItemPropertyValueFactory<FactoryProductionStep, Recipe>("recipe"));
		factoryIntermediariesTableIntermediaryItem.setCellValueFactory(new TreeItemPropertyValueFactory<FactoryProductionStep, Item>("itemProduced"));
		factoryIntermediariesTableCountPerSecond.setCellValueFactory(new TreeItemPropertyValueFactory<FactoryProductionStep, Double>("requiredIntermediariesPerSecond"));

		factoryInputsTable.setItems(calculatedInputsDatabase);
		factoryInputsTableItemNameColumn.setCellValueFactory(new PropertyValueFactory<FactoryInput, String>("itemName"));
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

		allRecipes.recipes.setAll(recipes);
		allItems.items.clear();

		allRecipes.recipes.forEach(recipe -> {
			recipe.getProducts(true).forEach((itemName, itemCount) -> {
				if (!allItems.contains(itemName)) {
					allItems.items.add(new Item(itemName));
				}
			});
		});

		System.out.println("Num recipies: " + allRecipes.recipes.size());
		System.out.println("Num Item Types: " + allItems.items.size());
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
		allItemsList.getSelectionModel().getSelectedItems().forEach(item -> {
			selectedOutputsDatabase.add(new FactoryOutput(item));
		});
	}

	@FXML
	private void onRemoveFactoryProductPressed()
	{
		// create copy of list so that we don't try to iterate and remove from our selectionModel at the same time!
		new ArrayList<FactoryOutput>(factoryOutputsTable.getSelectionModel().getSelectedItems()).forEach(item -> {
			selectedOutputsDatabase.remove(item);
		});
	}

	@FXML
	private void onAddOptionalFactoryInputPressed()
	{
		allItemsList.getSelectionModel().getSelectedItems().forEach(item -> {
			optionalInputsDatabase.add(item);
		});
	}

	@FXML
	private void onRemoveoptionalFactoryInputPressed()
	{
		// create copy of list so that we don't try to iterate and remove from our selectionModel at the same time!
		new ArrayList<Item>(optionalInputItemsList.getSelectionModel().getSelectedItems()).forEach(item -> {
			optionalInputsDatabase.remove(item);
		});
	}

	private void setTextEditToFilterListView(ListView<Item> listToFilter, ObservableList<Item> listItemsModel, TextField filterInput)
	{
		FilteredList<Item> filteredFactoryProductsList = new FilteredList<Item>(listItemsModel);

		listToFilter.setItems(new SortedList<Item>(filteredFactoryProductsList, (Item a, Item b) -> {
			return a.name.compareTo(b.name);
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

	private void filterItemList(FilteredList<Item> toFilter, String filterText)
	{
		toFilter.setPredicate(new Predicate<Item>()
		{
			@Override
			public boolean test(Item item)
			{
				return item.name.contains(filterText);
			}
		});
	}

	private void updateFactory()
	{
		ArrayList<FactoryProductionStep> intermediaries = new ArrayList<FactoryProductionStep>();

		// TODO allow for expensive recipes
		boolean useExpensiveRecipes = false;

		for (FactoryOutput product : selectedOutputsDatabase) {
			for (Recipe recipe : allRecipes.recipes) {
				if (recipe.getProducts(useExpensiveRecipes).containsKey(product.getName())) {
					intermediaries.add(new FactoryProductionStep(recipe, product.item, product.getProductionRatePerSecond(), allRecipes, allItems, optionalInputsDatabase, useExpensiveRecipes));
					// TODO do better then just using the first recipe we find
					break;
				}
			}
		}

		calculatedIntermediariesDatabase.setAll(intermediaries);

		calculatedInputsDatabase.clear();
		TreeItem<FactoryProductionStep> rootIntermediary = new TreeItem<FactoryProductionStep>();
		rootIntermediary.setExpanded(true);
		for (FactoryProductionStep topLevelIntermediary : intermediaries) {
			topLevelIntermediary.recursivelyAddIntermediariesToTree(rootIntermediary);
			topLevelIntermediary.recursivelyAccumulateFactoryInputs(calculatedInputsDatabase);
		}
		factoryIntermediariesTable.setRoot(rootIntermediary);
	}
}
