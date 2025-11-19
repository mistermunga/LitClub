package com.litclub.ui.main.shared.view.subcomponent.library;

import com.litclub.construct.Book;
import com.litclub.ui.main.shared.view.service.LibraryService;
import com.litclub.ui.main.shared.view.subcomponent.library.dialog.AddBookDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LibraryControlBar extends HBox {

    private final LibraryService libraryService;
    private final Consumer<Predicate<Book>> onFilterChange;
    private final Consumer<Comparator<Book>> onSortChange;

    private Label statsLabel;
    private MenuButton optionsMenu;
    private Button addBookButton;

    // Filter and sort state
    private String currentFilter = "All books";
    private String currentSort = "Recently added";

    /**
     * Creates a library control bar with filtering and sorting capabilities.
     *
     * @param libraryService the service for library operations
     * @param onFilterChange callback when filter changes (provides predicate)
     * @param onSortChange callback when sort changes (provides comparator)
     */
    public LibraryControlBar(LibraryService libraryService,
                             Consumer<Predicate<Book>> onFilterChange,
                             Consumer<Comparator<Book>> onSortChange) {
        this.libraryService = libraryService;
        this.onFilterChange = onFilterChange;
        this.onSortChange = onSortChange;

        this.getStyleClass().add("library-control-bar");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(15, 20, 15, 20));
        this.setSpacing(15);

        addStats();
        addSpacer();
        addButtons();

        // Listen for changes to update stats
        setupStatsListeners();
    }

    private void addStats() {
        statsLabel = new Label();
        statsLabel.getStyleClass().add("stats-label");
        updateStats();
        this.getChildren().add(statsLabel);
    }

    private void setupStatsListeners() {
        // Update stats whenever any book list changes
        libraryService.getAllBooks().addListener(
                (javafx.collections.ListChangeListener.Change<? extends Book> c) -> {
                    updateStats();
                }
        );
    }

    private void updateStats() {
        int totalBooks = libraryService.getTotalBookCount();
        int booksThisYear = libraryService.getBooksReadThisYear();

        // Calculate books per month average
        int currentMonth = LocalDate.now().getMonthValue();
        float rate = (float) booksThisYear / currentMonth;
        String formattedRate = String.format("%.1f", rate);

        statsLabel.setText("📚 " + totalBooks + " books  |  " +
                booksThisYear + " this year  |  " +
                formattedRate + " books/month");
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

        // Search item
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
        addBookButton.getStyleClass().addAll("button-primary", "add-book-button");
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
        searchItem.setHideOnClick(false);

        return searchItem;
    }

    private Menu createFilterMenu() {
        Menu filterMenu = new Menu("Filter by");
        ToggleGroup filterGroup = new ToggleGroup();

        String[] filters = {
                "All books",
                "Currently Reading",
                "Want to Read",
                "Finished"
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
                "Title (Z-A)",
                "Author (A-Z)",
                "Year (Newest)",
                "Year (Oldest)"
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

    // ==================== EVENT HANDLERS ====================

    private void handleSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Clear filter
            onFilterChange.accept(book -> true);
            return;
        }

        String lowerQuery = query.toLowerCase().trim();

        // Filter by title or author
        Predicate<Book> searchPredicate = book -> {
            String title = book.getTitle() != null ? book.getTitle().toLowerCase() : "";
            String author = book.getPrimaryAuthor() != null ? book.getPrimaryAuthor().toLowerCase() : "";

            return title.contains(lowerQuery) || author.contains(lowerQuery);
        };

        onFilterChange.accept(searchPredicate);
    }

    private void handleFilter(String filter) {
        currentFilter = filter;

        Predicate<Book> filterPredicate = switch (filter) {
            case "Currently Reading" -> book ->
                    libraryService.getCurrentlyReading().contains(book);
            case "Want to Read" -> book ->
                    libraryService.getWantToRead().contains(book);
            case "Finished" -> book ->
                    libraryService.getFinishedReading().contains(book);
            default -> book -> true; // "All books"
        };

        onFilterChange.accept(filterPredicate);
    }

    private void handleSort(String sortOption) {
        currentSort = sortOption;

        Comparator<Book> sortComparator = switch (sortOption) {
            case "Title (A-Z)" ->
                    Comparator.comparing(book -> book.getTitle() != null ? book.getTitle() : "");
            case "Title (Z-A)" ->
                    Comparator.comparing((Book book) -> book.getTitle() != null ? book.getTitle() : "").reversed();
            case "Author (A-Z)" ->
                    Comparator.comparing(Book::getPrimaryAuthor);
            case "Year (Newest)" ->
                    Comparator.comparing((Book book) -> book.getYear() != null ? book.getYear() : LocalDate.MIN).reversed();
            case "Year (Oldest)" ->
                    Comparator.comparing((Book book) -> book.getYear() != null ? book.getYear() : LocalDate.MAX);
            default -> // "Recently added" - keep current order (based on bookID)
                    Comparator.comparing((Book book) -> book.getBookID() != null ? book.getBookID() : 0L).reversed();
        };

        onSortChange.accept(sortComparator);
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
        AddBookDialog dialog = new AddBookDialog(libraryService);
        dialog.showAndWait().ifPresent(bookData -> {
            // Dialog returns book data, service handles the rest
            System.out.println("Book added via dialog: " + bookData.title());
        });
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Manually refresh stats (useful after external operations).
     */
    public void refreshStats() {
        updateStats();
    }

    /**
     * Get current filter setting.
     */
    public String getCurrentFilter() {
        return currentFilter;
    }

    /**
     * Get current sort setting.
     */
    public String getCurrentSort() {
        return currentSort;
    }

    /**
     * Enable/disable the add book button (e.g., during loading).
     */
    public void setAddBookButtonEnabled(boolean enabled) {
        addBookButton.setDisable(!enabled);
    }
}