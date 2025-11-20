package com.litclub.ui.main.shared.view.subcomponent.notes;

import com.litclub.construct.Note;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.event.EventBus.EventType;
import com.litclub.ui.main.shared.view.service.LibraryService;
import com.litclub.ui.main.shared.view.service.NoteService;
import com.litclub.ui.main.shared.view.subcomponent.notes.dialog.AddNoteDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Control bar for note views with filtering, sorting, and statistics.
 * Provides book-based filtering and various sort options.
 */
public class NoteControlBar extends HBox {

    private final NoteService noteService;
    private final boolean isPersonalContext;
    private final Consumer<Predicate<Note>> onFilterChange;
    private final Consumer<Comparator<Note>> onSortChange;

    private Label statsLabel;
    private MenuButton optionsMenu;
    private Button addNoteButton;

    // Filter and sort state
    private String currentFilter = "All notes";
    private String currentSort = "Recently added";

    /**
     * Creates a note control bar with filtering and sorting capabilities.
     *
     * @param noteService the service for note operations
     * @param isPersonalContext true if showing personal notes, false for club notes
     * @param onFilterChange callback when filter changes (provides predicate)
     * @param onSortChange callback when sort changes (provides comparator)
     */
    public NoteControlBar(NoteService noteService,
                          boolean isPersonalContext,
                          Consumer<Predicate<Note>> onFilterChange,
                          Consumer<Comparator<Note>> onSortChange) {
        this.noteService = noteService;
        this.isPersonalContext = isPersonalContext;
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
        // Update stats whenever note lists change
        if (isPersonalContext) {
            noteService.getPersonalNotes().addListener(
                    (javafx.collections.ListChangeListener.Change<? extends Note> c) -> {
                        updateStats();
                    }
            );
        } else {
            noteService.getClubNotes().addListener(
                    (javafx.collections.ListChangeListener.Change<? extends Note> c) -> {
                        updateStats();
                    }
            );
        }
    }

    private void updateStats() {
        int totalNotes = isPersonalContext
                ? noteService.getPersonalNotesCount()
                : noteService.getClubNotesCount();

        // Count unique books with notes
        long uniqueBooks = (isPersonalContext
                ? noteService.getPersonalNotes()
                : noteService.getClubNotes())
                .stream()
                .map(note -> note.getBook().getBookID())
                .distinct()
                .count();

        statsLabel.setText("📝 " + totalNotes + " notes  |  " +
                uniqueBooks + " books with notes");
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

        // Add Note Button
        addNoteButton = new Button("+ Add Note");
        addNoteButton.getStyleClass().addAll("button-primary", "add-book-button");
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
        searchItem.setHideOnClick(false);

        return searchItem;
    }

    private Menu createFilterMenu() {
        Menu filterMenu = new Menu("Filter by");
        ToggleGroup filterGroup = new ToggleGroup();

        // Get unique books from current notes
        var notes = isPersonalContext
                ? noteService.getPersonalNotes()
                : noteService.getClubNotes();

        // Add "All notes" option
        RadioMenuItem allItem = new RadioMenuItem("All notes");
        allItem.setToggleGroup(filterGroup);
        allItem.setOnAction(e -> handleFilter("All notes", null));
        allItem.setSelected(true);
        filterMenu.getItems().add(allItem);

        // Get unique books and create filter options
        notes.stream()
                .map(Note::getBook)
                .distinct()
                .sorted(Comparator.comparing(book -> book.getTitle() != null ? book.getTitle() : ""))
                .forEach(book -> {
                    String bookTitle = book.getTitle();
                    if (bookTitle != null && !bookTitle.isEmpty()) {
                        RadioMenuItem item = new RadioMenuItem(bookTitle);
                        item.setToggleGroup(filterGroup);
                        item.setOnAction(e -> handleFilter(bookTitle, book.getBookID()));
                        filterMenu.getItems().add(item);
                    }
                });

        return filterMenu;
    }

    private Menu createSortMenu() {
        Menu sortMenu = new Menu("Sort by");
        ToggleGroup sortGroup = new ToggleGroup();

        String[] sortOptions = {
                "Recently added",
                "Oldest first",
                "Book title (A-Z)",
                "Book title (Z-A)"
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
            onFilterChange.accept(note -> true);
            return;
        }

        String lowerQuery = query.toLowerCase().trim();

        // Filter by note content or book title
        Predicate<Note> searchPredicate = note -> {
            String content = note.getContent() != null ? note.getContent().toLowerCase() : "";
            String bookTitle = note.getBook() != null && note.getBook().getTitle() != null
                    ? note.getBook().getTitle().toLowerCase()
                    : "";

            return content.contains(lowerQuery) || bookTitle.contains(lowerQuery);
        };

        onFilterChange.accept(searchPredicate);
    }

    private void handleFilter(String filterName, Long bookID) {
        currentFilter = filterName;

        Predicate<Note> filterPredicate;

        if (bookID == null) {
            // "All notes"
            filterPredicate = note -> true;
        } else {
            // Filter by specific book
            filterPredicate = note -> note.getBook() != null
                    && note.getBook().getBookID().equals(bookID);
        }

        onFilterChange.accept(filterPredicate);
    }

    private void handleSort(String sortOption) {
        currentSort = sortOption;

        Comparator<Note> sortComparator = switch (sortOption) {
            case "Oldest first" ->
                    Comparator.comparing((Note note) -> note.getNoteID() != null ? note.getNoteID() : 0L);
            case "Book title (A-Z)" ->
                    Comparator.comparing(note -> {
                        if (note.getBook() == null || note.getBook().getTitle() == null) {
                            return "";
                        }
                        return note.getBook().getTitle();
                    });
            case "Book title (Z-A)" ->
                    Comparator.comparing((Note note) -> {
                        if (note.getBook() == null || note.getBook().getTitle() == null) {
                            return "";
                        }
                        return note.getBook().getTitle();
                    }).reversed();
            default -> // "Recently added"
                    Comparator.comparing((Note note) -> note.getNoteID() != null ? note.getNoteID() : 0L).reversed();
        };

        onSortChange.accept(sortComparator);
    }

    private void handleAddNote() {
        AddNoteDialog noteDialog = new AddNoteDialog(
                new LibraryService(),
                noteService,
                isPersonalContext
        );
        noteDialog.showAndWait().ifPresent(note -> {
            System.out.println("Note added via dialog");
        });
        EventType eventType = isPersonalContext ?
                EventType.PERSONAL_NOTES_UPDATED :
                EventType.CLUB_NOTES_UPDATED ;
        EventBus.getInstance().emit(eventType);
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Manually refresh stats (useful after external operations).
     */
    public void refreshStats() {
        updateStats();
    }

    /**
     * Refresh the filter menu to reflect current books.
     */
    public void refreshFilterMenu() {
        // Rebuild the options menu to update the filter list
        optionsMenu.getItems().clear();

        CustomMenuItem searchItem = createSearchItem();
        optionsMenu.getItems().add(searchItem);
        optionsMenu.getItems().add(new SeparatorMenuItem());

        Menu filterMenu = createFilterMenu();
        optionsMenu.getItems().add(filterMenu);

        Menu sortMenu = createSortMenu();
        optionsMenu.getItems().add(sortMenu);
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
     * Enable/disable the add note button (e.g., during loading).
     */
    public void setAddNoteButtonEnabled(boolean enabled) {
        addNoteButton.setDisable(!enabled);
    }
}