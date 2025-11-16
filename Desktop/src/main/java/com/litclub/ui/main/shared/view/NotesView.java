package com.litclub.ui.main.shared.view;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.subcomponent.notes.NoteControlBar;
import com.litclub.ui.main.shared.view.subcomponent.notes.NotesCore;
import javafx.scene.layout.BorderPane;

/**
 * Main notes view that combines the control bar and notes core.
 * Handles both personal and club notes contexts.
 */
public class NotesView extends BorderPane {

    private final NotesCore notesCore;
    private final NoteControlBar controlBar;
    private final boolean isPersonal;

    public NotesView(boolean isPersonal) {
        this.isPersonal = isPersonal;
        ThemeManager.getInstance().registerComponent(this);

        // Create core first
        notesCore = new NotesCore(isPersonal);

        // Create control bar with callbacks to core
        controlBar = new NoteControlBar(
                notesCore.getNoteService(),
                isPersonal,
                notesCore::applyFilter,  // Filter callback
                notesCore::applySort     // Sort callback
        );

        this.setTop(controlBar);
        this.setCenter(notesCore);
    }

    /**
     * Refresh the notes view.
     */
    public void refresh() {
        notesCore.refresh();
        controlBar.refreshStats();
    }

    /**
     * Navigate back to notes grid if currently viewing a focused note.
     */
    public void showNotesGrid() {
        if (notesCore.isViewingFocusedNote()) {
            notesCore.showNotesGrid();
        }
    }

    /**
     * Check if currently viewing a focused note.
     */
    public boolean isViewingFocusedNote() {
        return notesCore.isViewingFocusedNote();
    }
}