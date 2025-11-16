package com.litclub.ui.main.shared.view.subcomponent.notes;

import com.litclub.construct.Note;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.service.NoteService;
import com.litclub.ui.main.shared.view.subcomponent.notes.subview.DefaultNoteCore;
import com.litclub.ui.main.shared.view.subcomponent.notes.subview.NoteFocus;
import javafx.scene.layout.StackPane;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Core container for notes view that manages navigation between:
 * - DefaultNoteCore: Grid view of all notes
 * - NoteFocus: Detailed view of a single note with replies
 */
public class NotesCore extends StackPane {

    private final boolean isPersonal;
    private final DefaultNoteCore defaultNoteCore;
    private final NoteFocus noteFocus;

    public NotesCore(boolean isPersonal) {
        this.isPersonal = isPersonal;
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().add("notes-core");

        // Create subviews
        defaultNoteCore = new DefaultNoteCore(isPersonal, this::navigateToNote);
        noteFocus = new NoteFocus(isPersonal, this::navigateBack);

        // Add both to stack (only one visible at a time)
        this.getChildren().addAll(defaultNoteCore, noteFocus);

        // Show default view initially
        showDefaultView();
    }

    // ==================== NAVIGATION ====================

    /**
     * Navigate to focused view showing a specific note with replies.
     */
    private void navigateToNote(Note note) {
        System.out.println("Navigating to note: " + note.getNoteID());

        // Load the note into focus view
        noteFocus.loadNote(note);

        // Show focus view, hide default view
        defaultNoteCore.setVisible(false);
        noteFocus.setVisible(true);
    }

    /**
     * Navigate back to default grid view.
     */
    private void navigateBack() {
        System.out.println("Navigating back to notes list");

        // Show default view, hide focus view
        noteFocus.setVisible(false);
        defaultNoteCore.setVisible(true);

        // Refresh default view in case notes changed
        defaultNoteCore.refresh();
    }

    /**
     * Show default grid view.
     */
    private void showDefaultView() {
        defaultNoteCore.setVisible(true);
        noteFocus.setVisible(false);
    }

    // ==================== PUBLIC API (for NoteControlBar) ====================

    /**
     * Apply filter predicate to the default view.
     */
    public void applyFilter(Predicate<Note> filterPredicate) {
        defaultNoteCore.applyFilter(filterPredicate);
    }

    /**
     * Apply sort comparator to the default view.
     */
    public void applySort(Comparator<Note> sortComparator) {
        defaultNoteCore.applySort(sortComparator);
    }

    /**
     * Refresh the notes view.
     */
    public void refresh() {
        defaultNoteCore.refresh();
    }

    /**
     * Get the note service instance from default core.
     */
    public NoteService getNoteService() {
        return defaultNoteCore.getNoteService();
    }

    /**
     * Check if currently viewing a focused note.
     */
    public boolean isViewingFocusedNote() {
        return noteFocus.isVisible();
    }

    /**
     * Navigate back to default view (public method for external navigation).
     */
    public void showNotesGrid() {
        navigateBack();
    }
}