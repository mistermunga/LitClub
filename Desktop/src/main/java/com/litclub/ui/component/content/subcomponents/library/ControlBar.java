package com.litclub.ui.component.content.subcomponents.library;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Calendar;

public class ControlBar extends HBox {

    private MenuButton optionsMenu;
    private Button addBookButton;

    // Filter and sort state
    private String currentFilter = "All books";
    private String currentSort = "Recently added";

    public ControlBar() {
        this.getStyleClass().add("library-control-bar");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(15, 20, 15, 20));
        this.setSpacing(15);

        addStats();
        addSpacer();
        addButtons();
    }

    //TODO Implement API calls here; hardcoded data for now
    private void addStats() {
        Label stats = new Label();
        int bookCount = 150;
        int booksForCurrentYear = 8;
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;

        // Calculate books per month average
        float rate = month > 0 ? (float) booksForCurrentYear / month : 0;
        String formattedRate = String.format("%.1f", rate);

        stats.setText("📚 " + bookCount + " books  |  " +
                booksForCurrentYear + " this year  |  " +
                formattedRate + " books/month");
        stats.getStyleClass().add("stats-label");

        this.getChildren().add(stats);
    }

    private void addSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        this.getChildren().add(spacer);
    }

    private void addButtons() {
        HBox buttonGroup = new HBox(10);
        buttonGroup.setAlignment(Pos.CENTER_RIGHT);

        // Options Menu Button
        optionsMenu = new MenuButton("⚙ Options");
        optionsMenu.getStyleClass().add("options-menu");

        // Search item (custom with text field)
        CustomMenuItem searchItem = createSearchItem();
        optionsMenu.getItems().add(searchItem);
        optionsMenu.getItems().add(new SeparatorMenuItem());

        // Filter submenu
        Menu filterMenu = createFilterMenu();
        optionsMenu.getItems().add(filterMenu);

        // Sort submenu
        Menu sortMenu = createSortMenu();
        optionsMenu.getItems().add(sortMenu);

        optionsMenu.getItems().add(new SeparatorMenuItem());

        // View toggle
        MenuItem viewToggle = new MenuItem("Switch to List View");
        viewToggle.setOnAction(e -> handleViewToggle(viewToggle));
        optionsMenu.getItems().add(viewToggle);

        // Add Book Button
        addBookButton = new Button("+ Add Book");
        addBookButton.getStyleClass().addAll("button-primary");
        addBookButton.setOnAction(e -> handleAddBook());

        buttonGroup.getChildren().addAll(optionsMenu, addBookButton);
        this.getChildren().add(buttonGroup);
    }

    private CustomMenuItem createSearchItem() {
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search books...");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("search-field");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            handleSearch(newVal);
        });

        CustomMenuItem searchItem = new CustomMenuItem(searchField);
        searchItem.setHideOnClick(false); // Keep menu open while typing

        return searchItem;
    }

    private Menu createFilterMenu() {
        Menu filterMenu = new Menu("Filter by");

        // Create radio menu items for exclusive selection
        ToggleGroup filterGroup = new ToggleGroup();

        String[] filters = {
                "All books",
                "Genre",
                "Author",
                "Club",
                "Year"
        };

        for (String filter : filters) {
            RadioMenuItem item = new RadioMenuItem(filter);
            item.setToggleGroup(filterGroup);
            item.setOnAction(e -> handleFilter(filter));

            if (filter.equals(currentFilter)) {
                item.setSelected(true);
            }

            filterMenu.getItems().add(item);
        }

        return filterMenu;
    }

    private Menu createSortMenu() {
        Menu sortMenu = new Menu("Sort by");

        ToggleGroup sortGroup = new ToggleGroup();

        String[] sortOptions = {
                "Recently added",
                "Title (A-Z)",
                "Author",
                "Date read",
                "Rating"
        };

        for (String option : sortOptions) {
            RadioMenuItem item = new RadioMenuItem(option);
            item.setToggleGroup(sortGroup);
            item.setOnAction(e -> handleSort(option));

            if (option.equals(currentSort)) {
                item.setSelected(true);
            }

            sortMenu.getItems().add(item);
        }

        return sortMenu;
    }

    // Event Handlers
    private void handleSearch(String query) {
        System.out.println("Searching for: " + query);
        // TODO: Implement search functionality
        // This should filter the book display based on the query
    }

    private void handleFilter(String filter) {
        currentFilter = filter;
        System.out.println("Filter changed to: " + filter);
        // TODO: Implement filter logic
        // May need to show additional UI for genre/author/club/year selection
    }

    private void handleSort(String sortOption) {
        currentSort = sortOption;
        System.out.println("Sort changed to: " + sortOption);
        // TODO: Implement sort logic
        // This should re-order the displayed books
    }

    private void handleViewToggle(MenuItem item) {
        if (item.getText().contains("List")) {
            item.setText("Switch to Dashboard View");
            System.out.println("Switched to List View");
            // TODO: Trigger view change to list layout
        } else {
            item.setText("Switch to List View");
            System.out.println("Switched to Dashboard View");
            // TODO: Trigger view change to dashboard layout
        }
    }

    private void handleAddBook() {
        System.out.println("Add Book button clicked");
        // TODO: Open the book management scene/dialog
        // SceneManager.getInstance().showBookManagement();
    }

    // Public methods to trigger actions from parent components
    public void refreshStats(int totalBooks, int thisYear, float rate) {
        // Update stats label dynamically
    }

    public String getCurrentFilter() {
        return currentFilter;
    }

    public String getCurrentSort() {
        return currentSort;
    }
}