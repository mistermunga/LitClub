package com.litclub.ui.component.content.subcomponents.notes.atoms;

import com.litclub.persistence.DataRepository;
import com.litclub.session.construct.Note;
import com.litclub.theme.ThemeManager;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class FilteredNotesCore extends DefaultNotesCore {

    private final VBox container;
    private final DataRepository repository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, yyyy");

    public FilteredNotesCore(String FILTER) {
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("notes-core", "scroll-pane");

        this.repository = DataRepository.getInstance();
        this.container = new VBox(30);
        this.container.setPadding(new Insets(20));
        this.container.getStyleClass().add("container");

        this.setContent(container);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        applyFilter(FILTER);
    }

    private void applyFilter(String filter) {
        var allNotes = repository.getNotes();
        FilteredList<Note> filteredList = new FilteredList<>(allNotes);

        switch (filter) {
            case "Private only" -> filteredList.setPredicate(Note::isPrivate);
            case "Club notes only" -> filteredList.setPredicate(note -> !note.isPrivate());
            default -> filteredList.setPredicate(note -> true);
        }

        FlowPane noteCards = super.createNoteCards(filteredList);
        container.getChildren().add(noteCards);
    }
}
