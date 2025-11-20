package com.litclub.ui.main.shared.view;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.event.EventBus.EventType;
import com.litclub.ui.main.shared.view.subcomponent.library.LibraryControlBar;
import com.litclub.ui.main.shared.view.subcomponent.library.LibraryCore;
import javafx.scene.layout.BorderPane;

import java.util.Set;

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

        Set<EventType> relevantEvents = Set.of(
                EventType.BOOKS_UPDATED,
                EventType.PERSONAL_LIBRARY_UPDATED
        );

        for (EventType eventType : relevantEvents) {
            EventBus.getInstance().on(eventType, this::refresh);
        }
    }

    /**
     * Refresh the library view.
     */
    public void refresh() {
        libraryCore.refresh();
        controlBar.refreshStats();
    }
}
