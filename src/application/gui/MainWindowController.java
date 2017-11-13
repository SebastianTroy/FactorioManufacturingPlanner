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
import application.manufacturingPlanner.FactoryOutputsModel;
import application.manufacturingPlanner.FactoryProductionStep;
import application.manufacturingPlanner.Item;
import application.manufacturingPlanner.ItemsDatabase;
import application.manufacturingPlanner.Recipe;
import application.manufacturingPlanner.RecipesDatabase;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.VBox;

public class MainWindowController
{
	@FXML
	VBox root;
	
	// ----- menu bar -----
	@FXML
	MenuItem loadRecipiesButton;
	@FXML
	MenuItem defaultCssButton;
	@FXML
	MenuItem darkCssButton;

	// ----- accordian panes -----
	@FXML
	TitledPane factorySettingsPane;
	@FXML
	TitledPane factoryDetailsPane;

	// ----- factory setup -----
	@FXML
	CheckBox useExpensiveRecipesCheckBox;
	
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
	TableColumn<FactoryOutput, DoubleProperty> factoryOutputsTableProductionRateColumn;
	@FXML
	TableColumn<FactoryOutput, ObjectProperty<FactoryOutput.ProductionRateUnit>> factoryOutputsTableProductionRateUnitColumn;

	@FXML
	ListView<Item> optionalInputItemsList;
	@FXML
	Button addOptionalItemInput;
	@FXML
	Button removeOptionalItemInput;

	// ----- factory details -----
	@FXML
	TreeTableView<FactoryProductionStep> factoryProductionStepsTable;
	@FXML
	TreeTableColumn<FactoryProductionStep, Recipe> factoryProductionStepsRecipeColumn;
	@FXML
	TreeTableColumn<FactoryProductionStep, Item> factoryProductionStepsItemColumn;
	@FXML
	TreeTableColumn<FactoryProductionStep, Double> factoryIntermediariesTableCountPerSecond;

	@FXML
	TableView<FactoryInput> factoryInputsTable;
	@FXML
	TableColumn<FactoryInput, Item> factoryInputsTableItemColumn;
	@FXML
	TableColumn<FactoryInput, Double> factoryInputsTableInputRateColumn;

	private RecipesDatabase allRecipes = new RecipesDatabase();
	private ItemsDatabase allItems = new ItemsDatabase();
	private ObservableList<Item> optionalInputsDatabase = FXCollections.observableArrayList();
	FactoryOutputsModel selectedOutputsModel = new FactoryOutputsModel();
	private ObservableList<FactoryProductionStep> calculatedIntermediariesDatabase = FXCollections.observableArrayList();
	private ObservableList<FactoryInput> calculatedInputsDatabase = FXCollections.observableArrayList();

	@FXML
	void initialize()
	{
		Platform.runLater(() -> factorySettingsPane.setExpanded(true));
		Platform.runLater(() -> onDefaultCssClicked());

		setTextEditToFilterListView(allItemsList, allItems.items, allItemsFilter);

		selectedOutputsModel.addModelUpdatedListener(() -> updateFactory());

		optionalInputsDatabase.addListener((ListChangeListener.Change<? extends Item> c) -> {
			updateFactory();
		});

		allItemsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		factoryOutputsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		optionalInputItemsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		factoryOutputsTable.setItems(selectedOutputsModel.getfactoryOutputs());
		factoryOutputsTableProductNameColumn.setCellValueFactory(new PropertyValueFactory<FactoryOutput, String>("name"));
		factoryOutputsTableProductionRateColumn.setCellValueFactory(i -> {
			final DoubleProperty value = i.getValue().productionRateProperty();
			return Bindings.createObjectBinding(() -> value);
		});
		factoryOutputsTableProductionRateColumn.setCellFactory(col -> {
			TableCell<FactoryOutput, DoubleProperty> cell = new TableCell<FactoryOutput, DoubleProperty>();
			cell.setEditable(true);
			final Spinner<Double> spinner = new Spinner<Double>(0, Double.MAX_VALUE, 1);
			spinner.setEditable(true);
			cell.itemProperty().addListener((observable, oldValue, newValue) -> {
				if (oldValue != null) {
					spinner.getValueFactory().valueProperty().unbindBidirectional(oldValue.asObject());
				}
				if (newValue != null) {
					spinner.getValueFactory().valueProperty().bindBidirectional(newValue.asObject());
					newValue.addListener((obs, o, n) -> {
						updateFactory();
					});
				}
			});

			cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(spinner));
			return cell;
		});
		factoryOutputsTableProductionRateUnitColumn.setCellValueFactory(i -> {
			final ObjectProperty<FactoryOutput.ProductionRateUnit> value = i.getValue().productionRateUnitProperty();
			return Bindings.createObjectBinding(() -> value);
		});
		factoryOutputsTableProductionRateUnitColumn.setCellFactory(col -> {
			TableCell<FactoryOutput, ObjectProperty<FactoryOutput.ProductionRateUnit>> cell = new TableCell<FactoryOutput, ObjectProperty<FactoryOutput.ProductionRateUnit>>();
			final ComboBox<FactoryOutput.ProductionRateUnit> comboBox = new ComboBox<FactoryOutput.ProductionRateUnit>(FXCollections.observableArrayList(FactoryOutput.ProductionRateUnit.values()));
			cell.itemProperty().addListener((observable, oldValue, newValue) -> {
				if (oldValue != null) {
					comboBox.valueProperty().unbindBidirectional(oldValue);
				}
				if (newValue != null) {
					comboBox.valueProperty().bindBidirectional(newValue);
					newValue.addListener((obs, o, n) -> {
						updateFactory();
					});
				}
			});
			cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(comboBox));
			return cell;
		});

		optionalInputItemsList.setItems(optionalInputsDatabase);

		factoryProductionStepsTable.setShowRoot(false);
		factoryProductionStepsRecipeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<FactoryProductionStep, Recipe>("recipe"));
		factoryProductionStepsItemColumn.setCellValueFactory(new TreeItemPropertyValueFactory<FactoryProductionStep, Item>("itemProduced"));
		factoryIntermediariesTableCountPerSecond.setCellValueFactory(new TreeItemPropertyValueFactory<FactoryProductionStep, Double>("requiredIntermediariesPerSecond"));
		factoryIntermediariesTableCountPerSecond.setStyle( "-fx-alignment: CENTER-RIGHT;");
		factoryIntermediariesTableCountPerSecond.setCellFactory(col -> 
		new TreeTableCell<FactoryProductionStep, Double>() {
			@Override 
			public void updateItem(Double requiredIntermediariesPerSecond, boolean empty) {
				super.updateItem(requiredIntermediariesPerSecond, empty);
				if (empty) {
					setText(null);
				} else {
					setText(String.format("%.2f", requiredIntermediariesPerSecond.doubleValue()));
				}
			}
		});

		factoryInputsTable.setItems(calculatedInputsDatabase);
		factoryInputsTableItemColumn.setCellValueFactory(new PropertyValueFactory<FactoryInput, Item>("item"));
		factoryInputsTableInputRateColumn.setCellValueFactory(new PropertyValueFactory<FactoryInput, Double>("itemsPerSecond"));
		factoryInputsTableInputRateColumn.setStyle( "-fx-alignment: CENTER-RIGHT;");
		factoryInputsTableInputRateColumn.setCellFactory(col -> 
		new TableCell<FactoryInput, Double>() {
			@Override 
			public void updateItem(Double itemsPerSecond, boolean empty) {
				super.updateItem(itemsPerSecond, empty);
				if (empty) {
					setText(null);
				} else {
					setText(String.format("%.2f", itemsPerSecond.doubleValue()));
				}
			}
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
						recipes.addAll(recipesParser.parseRecipies(recipesFile, allItems));
					} catch (IOException | ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}

		allRecipes.recipes.setAll(recipes);
	}

	@FXML
	private void onDefaultCssClicked()
	{
		root.getScene().getStylesheets().remove(getClass().getResource("dark.css").toExternalForm());
		root.getScene().getStylesheets().add(getClass().getResource("default.css").toExternalForm());
	}

	@FXML
	private void onDarkCssClicked()
	{
		root.getScene().getStylesheets().remove(getClass().getResource("default.css").toExternalForm());
		root.getScene().getStylesheets().add(getClass().getResource("dark.css").toExternalForm());
	}

	@FXML
	private void onAddFactoryProductPressed()
	{
		allItemsList.getSelectionModel().getSelectedItems().forEach(item -> {
			selectedOutputsModel.addNewOutput(item);
		});
	}

	@FXML
	private void onRemoveFactoryProductPressed()
	{
		// create copy of list so that we don't try to iterate and remove from our selectionModel at the same time!
		new ArrayList<FactoryOutput>(factoryOutputsTable.getSelectionModel().getSelectedItems()).forEach(output -> {
			selectedOutputsModel.removeOutput(output.item);
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

	@FXML
	private void updateFactory()
	{
		ArrayList<FactoryProductionStep> intermediaries = new ArrayList<FactoryProductionStep>();

		for (FactoryOutput product : selectedOutputsModel.getfactoryOutputs()) {
			intermediaries.add(new FactoryProductionStep(product.item, product.getProductionRatePerSecond(), allRecipes, optionalInputsDatabase, useExpensiveRecipesCheckBox.isSelected()));
		}

		calculatedIntermediariesDatabase.setAll(intermediaries);

		calculatedInputsDatabase.clear();
		TreeItem<FactoryProductionStep> rootIntermediary = new TreeItem<FactoryProductionStep>();
		rootIntermediary.setExpanded(true);
		for (FactoryProductionStep topLevelIntermediary : intermediaries) {
			topLevelIntermediary.recursivelyBuildProductionDependancyTree(rootIntermediary);
			topLevelIntermediary.recursivelyAccumulateFactoryInputs(calculatedInputsDatabase);
		}
		factoryProductionStepsTable.setRoot(rootIntermediary);
	}
}