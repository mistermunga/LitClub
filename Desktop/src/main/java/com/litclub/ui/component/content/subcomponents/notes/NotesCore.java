package com.litclub.ui.component.content.subcomponents.notes;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.content.subcomponents.notes.atoms.DefaultNotesCore;
import javafx.scene.layout.StackPane;

public class NotesCore extends StackPane {

    private final DefaultNotesCore defaultNotesCore;

    public NotesCore() {
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().add("root");

        // Create and add the default notes view
        this.defaultNotesCore = new DefaultNotesCore();
        this.getChildren().add(defaultNotesCore);
    }

    public DefaultNotesCore getDefaultNotesCore() {
        return defaultNotesCore;
    }

    public void pushAtom(javafx.scene.Node atom) {
        if (!getChildren().contains(atom)) {
            getChildren().add(atom);
        }
    }

    public void removeAtom(javafx.scene.Node atom) {
        getChildren().remove(atom);
    }

    public void clearAtoms() {
        getChildren().retainAll(defaultNotesCore);
    }
}
