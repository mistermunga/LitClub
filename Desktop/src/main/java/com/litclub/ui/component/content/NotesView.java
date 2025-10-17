package com.litclub.ui.component.content;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.content.subcomponents.notes.NoteControlBar;
import com.litclub.ui.component.content.subcomponents.notes.NotesCore;
import javafx.scene.layout.BorderPane;

public class NotesView extends BorderPane {

    public NotesView() {
        ThemeManager.getInstance().registerComponent(this);
        showControlBar();
        showNotesCore();
    }

    public void showControlBar() {
        NoteControlBar controlBar = new NoteControlBar();
        this.setTop(controlBar);
    }

    public void showNotesCore() {
        NotesCore notesCore = new NotesCore();
        this.setCenter(notesCore);
    }
}