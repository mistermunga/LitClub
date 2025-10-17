package com.litclub.ui.component.content;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.content.subcomponents.library.LibraryControlBar;
import com.litclub.ui.component.content.subcomponents.library.LibraryCore;
import javafx.scene.layout.BorderPane;

public class LibraryView extends BorderPane {

    public LibraryView() {
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
