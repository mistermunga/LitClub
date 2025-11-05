package com.litclub.ui.component.content.subcomponents.notes.atoms.functionality;

import com.litclub.construct.Note;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.regex.Pattern;

public class NoteFilter {

    private ObservableList<Note> notes;

    public NoteFilter(ObservableList<Note> notes) {
        this.notes = notes;
    }

    public ObservableList<Note> applySearch(String searchText) {
        ObservableList<Note> filteredList = FXCollections.observableArrayList();

        String regex = "(?i).*" + Pattern.quote(searchText.trim()) + ".*";

        for (Note note : this.notes) {
            String content = note.getContent() != null ? note.getContent().trim() : "";
            String bookTitle = note.getBookTitle() != null ? note.getBookTitle().trim() : "";
            String bookAuthor = note.getAuthorName() != null ? note.getAuthorName().trim() : "";

            if (content.matches(regex) || bookTitle.matches(regex) || bookAuthor.matches(regex)) {
                filteredList.add(note);
            }
        }

        return filteredList;
    }

    public FilteredList<Note> applyFilter(String filterText) {
        FilteredList<Note> filteredList = new FilteredList<>(notes);

        switch (filterText) {
            case "Private only" -> filteredList.setPredicate(Note::isPrivate);
            case "Club notes only" -> filteredList.setPredicate(note -> !note.isPrivate());
            default -> filteredList.setPredicate(note -> true);
        }

        return filteredList;
    }

}
