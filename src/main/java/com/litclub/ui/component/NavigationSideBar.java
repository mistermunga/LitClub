package com.litclub.ui.component;

import com.litclub.session.AppSession;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class NavigationSideBar extends VBox {

    ContentArea contentArea;

    public NavigationSideBar(ContentArea contentArea) {
        this.contentArea = contentArea;
        this.setSpacing(8);
        this.setPadding(new Insets(20, 15, 20, 15));
        this.setMinWidth(200);
        this.setMaxWidth(200);
        this.getStyleClass().add("nav-sidebar");

        // Add nav buttons
        addNavButton("🏠", "Home");
        addNavButton("📚", "Library");
        addNavButton("📅", "Meetings");
        addNavButton("📝", "Notes");
        addNavButton("✨", "Recommendations");

        if (AppSession.getInstance().getClubRecord().administrator()) {
            addNavButton("👥", "Members");
        }
    }

    private void addNavButton(String emoji, String text) {
        Button button = new Button();
        button.setText(emoji + " " + text);
        button.getStyleClass().add("nav-button");

        button.setOnAction(
                event -> {
                    contentArea.showView(text);
                }
        );

        this.getChildren().add(button);
    }
}