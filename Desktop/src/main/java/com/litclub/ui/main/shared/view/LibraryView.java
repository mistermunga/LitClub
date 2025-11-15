package com.litclub.ui.main.shared.view;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.subcomponent.library.LibraryControlBar;
import com.litclub.ui.main.shared.view.subcomponent.library.LibraryCore;
import javafx.scene.layout.BorderPane;

public class LibraryView extends BorderPane {

    private final LibraryCore libraryCore;
    private final LibraryControlBar controlBar;

    public LibraryView(boolean isPersonal) {
        ThemeManager.getInstance().registerComponent(this);

        // Create core first
        libraryCore = new LibraryCore();

        // Create control bar with callbacks to core
        controlBar = new LibraryControlBar(
                libraryCore.getLibraryService(),
                libraryCore::applyFilter,  // Filter callback
                libraryCore::applySort     // Sort callback
        );

        this.setTop(controlBar);
        this.setCenter(libraryCore);
    }

    /**
     * Refresh the library view.
     */
    public void refresh() {
        libraryCore.refresh();
        controlBar.refreshStats();
    }
}
