package com.litclub.ui.component.content.subcomponents.notes.atoms.functionality;

import com.litclub.construct.Note;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;

public class NoteSort {

    public NoteSort() {
        // Stateless - operates on passed lists
    }

    /**
     * Applies sorting to a list of notes.
     * Creates a new sorted ObservableList to avoid modifying input lists in-place.
     *
     * @param sortType The type of sort to apply
     * @param notes The list to sort
     * @return A new sorted ObservableList
     */
    public ObservableList<Note> applySort(String sortType, ObservableList<Note> notes) {
        // Create a new mutable copy - critical for FilteredList compatibility
        ObservableList<Note> sortedList = FXCollections.observableArrayList(notes);

        switch (sortType) {
            case "Recently created" ->
                    sortedList.sort(Comparator.comparing(Note::getCreatedAt).reversed());

            case "Oldest first" ->
                    sortedList.sort(Comparator.comparing(Note::getCreatedAt));

            case "Book title (A-Z)" ->
                    sortedList.sort(Comparator.comparing(
                            Note::getBookTitle,
                            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                    ));

            case "Most relevant" ->
                // Default to recently created for now
                // TODO: Implement relevance scoring based on search query match
                    sortedList.sort(Comparator.comparing(Note::getCreatedAt).reversed());

            default ->
                    sortedList.sort(Comparator.comparing(Note::getCreatedAt).reversed());
        }

        return sortedList;
    }
}