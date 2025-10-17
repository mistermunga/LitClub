package com.litclub.ui.component.content.subcomponents.notes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class NoteControlBar extends HBox {

    private MenuButton optionsMenu;
    private Button addNoteButton;

    // Filter and sort state
    private String currentFilter = "All notes";
    private String currentSort = "Recently created";

    public NoteControlBar() {
        this.getStyleClass().add("library-control-bar");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(15, 20, 15, 20));
        this.setSpacing(15);

        addStats();
        addSpacer();
        addButtons();
    }

    // TODO: Implement API calls here; currently showing placeholder stats
    private void addStats() {
        Label stats = new Label();
        int totalNotes = 47;
        int privateNotes = 12;
        int clubNotes = 35;

        stats.setText("📝 " + totalNotes + " notes  |  " +
                privateNotes + " private  |  " +
                clubNotes + " shared");
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

        // View toggle (optional for notes)
        MenuItem viewToggle = new MenuItem("Switch to List View");
        viewToggle.setOnAction(e -> handleViewToggle(viewToggle));
        optionsMenu.getItems().add(viewToggle);

        // Add Note Button
        addNoteButton = new Button("+ Add Note");
        addNoteButton.getStyleClass().addAll("button-primary");
        addNoteButton.setOnAction(e -> handleAddNote());

        buttonGroup.getChildren().addAll(optionsMenu, addNoteButton);
        this.getChildren().add(buttonGroup);
    }

    private CustomMenuItem createSearchItem() {
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search notes...");
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

        ToggleGroup filterGroup = new ToggleGroup();

        String[] filters = {
                "All notes",
                "Private only",
                "Club notes only",
                "By book",
                "By date"
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
                "Recently created",
                "Oldest first",
                "Book title (A-Z)",
                "Most relevant"
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
        System.out.println("Searching notes for: " + query);
        // TODO: Implement search functionality
    }

    private void handleFilter(String filter) {
        currentFilter = filter;
        System.out.println("Note filter changed to: " + filter);
        // TODO: Implement filter logic
    }

    private void handleSort(String sortOption) {
        currentSort = sortOption;
        System.out.println("Note sort changed to: " + sortOption);
        // TODO: Implement sort logic
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

    private void handleAddNote() {
        System.out.println("Add Note button clicked");
        // TODO: Open the note creation dialog/scene
    }

    // Public methods for parent component
    public String getCurrentFilter() {
        return currentFilter;
    }

    public String getCurrentSort() {
        return currentSort;
    }
}