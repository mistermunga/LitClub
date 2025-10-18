package com.litclub.ui.component.content;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.content.subcomponents.notes.NoteControlBar;
import com.litclub.ui.component.content.subcomponents.notes.NotesCore;
import javafx.scene.layout.BorderPane;

public class NotesView extends BorderPane {

    private NotesCore notesCore;
    private NoteControlBar controlBar;

    public NotesView() {
        ThemeManager.getInstance().registerComponent(this);
        showNotesCore();
        showControlBar();
    }

    public void showControlBar() {
        NoteControlBar controlBar = new NoteControlBar();
        this.controlBar = controlBar;
        this.setTop(controlBar);
    }

    public void showNotesCore() {
        NotesCore notesCore = new NotesCore();
        this.notesCore = notesCore;
        this.setCenter(notesCore);
    }
}