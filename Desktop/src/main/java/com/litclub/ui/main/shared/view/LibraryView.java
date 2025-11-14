package com.litclub.ui.main.shared.view;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.subcomponent.library.LibraryControlBar;
import com.litclub.ui.main.shared.view.subcomponent.library.LibraryCore;
import javafx.scene.layout.BorderPane;

public class LibraryView extends BorderPane {

    public LibraryView(boolean isPersonal) {
        ThemeManager.getInstance().registerComponent(this);
        showControlBar();
        showLibraryCore();
    }

    public void showControlBar() {
        LibraryControlBar libraryControlBar = new LibraryControlBar();
        this.setTop(libraryControlBar);
    }

    public void showLibraryCore() {
        LibraryCore libraryCore = new LibraryCore();
        this.setCenter(libraryCore);
    }
}
