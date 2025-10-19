package com.litclub.ui.component.content.subcomponents.notes.atoms;

import com.litclub.persistence.DataRepository;
import com.litclub.session.construct.Note;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.content.subcomponents.notes.atoms.functionality.NoteFilter;
import com.litclub.ui.component.content.subcomponents.notes.atoms.functionality.NoteSort;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.FlowPane;
import java.util.Set;

public class FilteredNotesCore extends DefaultNotesCore {

    private final NoteFilter noteFilter;
    private final NoteSort noteSort;
    private final ObservableList<Note> notes;
    private String currentSort = "Recently created";
    private String currentFilter = "All notes";
    private String currentQuery = "";
    private String internalState = "";
    private ObservableList<Note> observableNotes = FXCollections.observableArrayList();

    public FilteredNotesCore(String filterorsearch) {
        super();
        container.getChildren().clear();

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("notes-core", "scroll-pane");

        DataRepository repository = super.dataRepository;

        this.notes = repository.getNotes();
        this.noteFilter = new NoteFilter(this.notes);
        this.noteSort = new NoteSort();

        this.setContent(container);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        Set<String> filterOptions = Set.of(
                "All notes",
                "Private only",
                "Club notes only"
        );

        if (filterOptions.contains(filterorsearch)){
            applyFilter(filterorsearch);
        } else {
            applySearch(filterorsearch);
        }
    }

    private void applyFilter(String filter) {
        internalState = "filter";

        // Clear previous results to prevent duplication
        observableNotes.clear();
        container.getChildren().clear();

        FilteredList<Note> filteredList = noteFilter.applyFilter(filter);
        observableNotes.addAll(filteredList);

        this.currentFilter = filter.equals("")
                ? "All notes"
                : filter;

        ObservableList<Note> displayList = noteSort.applySort(currentSort, observableNotes);

        FlowPane noteCards = super.createNoteCards(displayList);
        container.getChildren().add(noteCards);
    }

    public void applySearch(String query) {
        internalState = "search";
        currentQuery = query;

        if (query == null || query.trim().isEmpty()) {
            applyFilter("All notes");
            return;
        }

        // Clear previous results to prevent duplication
        observableNotes.clear();
        container.getChildren().clear();

        ObservableList<Note> searchResults = noteFilter.applySearch(query);
        observableNotes.addAll(searchResults);

        ObservableList<Note> displayList = noteSort.applySort(currentSort, observableNotes);

        FlowPane noteCards = super.createNoteCards(displayList);
        container.getChildren().add(noteCards);
    }

    public void updateSort(String sort) {
        Set<String> sortOptions = Set.of(
                "Recently created",
                "Oldest first",
                "Book title (A-Z)"
        );

        if (!sortOptions.contains(sort)) {
            return;
        }

        currentSort = sort;

        // Re-apply the current filter/search with the new sort
        switch (internalState) {
            case "filter" -> applyFilter(currentFilter);
            case "search" -> applySearch(currentQuery);
            default -> applyFilter("All notes");
        }
    }
}