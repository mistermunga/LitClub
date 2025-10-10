package com.litclub.ui.component;

import com.litclub.session.AppSession;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

public class NavigationSideBar extends VBox {

    public NavigationSideBar() {
        this.setSpacing(8);
        this.setPadding(new Insets(20, 15, 20, 15));
        this.setMinWidth(200);
        this.setMaxWidth(200);
        this.getStyleClass().add("navigation-sidebar");

        // Add nav buttons
        addNavButton("🏠", "Home");
        addNavButton("📚", "Library");
        addNavButton("📅", "Meetings");
        addNavButton("📝", "Notes");
        addNavButton("✨", "Recommendations");

        // Conditionally add admin-only button
        if (AppSession.getInstance().getClubRecord().administrator()) {
            addNavButton("👥", "Members");
        }
    }

    private void addNavButton(String emoji, String text) {}
}
