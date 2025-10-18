package com.litclub.ui.component.content.subcomponents.notes.atoms;

import com.litclub.persistence.DataRepository;
import com.litclub.session.construct.Note;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.content.subcomponents.notes.atoms.functionality.NoteFilter;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.FlowPane;
import java.util.Set;

public class FilteredNotesCore extends DefaultNotesCore {

    private final NoteFilter noteFilter;
    ObservableList<Note> notes;

    public FilteredNotesCore(String FILTERorSEARCH) {
        super();
        container.getChildren().clear();

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("notes-core", "scroll-pane");

        DataRepository repository = super.dataRepository;

        this.notes = repository.getNotes();
        this.noteFilter = new NoteFilter(this.notes);

        this.setContent(container);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        Set<String> searchOptions = Set.of("All notes",
                "Private only",
                "Club notes only"
        );

        if (searchOptions.contains(FILTERorSEARCH)){
            applyFilter(FILTERorSEARCH);
        } else {
            applySearch(FILTERorSEARCH);
        }

    }

    private void applyFilter(String filter) {
        FilteredList<Note> filteredList = noteFilter.applyFilter(filter);

        FlowPane noteCards = super.createNoteCards(filteredList);
        container.getChildren().add(noteCards);
    }

    public void applySearch(String query) {
        if (query == null) {
            applyFilter("");
            return;
        }
        ObservableList<Note> filteredList = noteFilter.applySearch(query);
        FlowPane noteCards = super.createNoteCards(filteredList);
        container.getChildren().add(noteCards);
    }
}
