package com.litclub.ui.main.shared;

import com.litclub.construct.enums.ClubRole;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeToggleBar;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

public class NavigationSideBar extends VBox {

    private final ContentArea contentArea;
    private final AppSession session = AppSession.getInstance();

    public NavigationSideBar(ContentArea contentArea, boolean isPersonal) {
        this.contentArea = contentArea;

        setSpacing(8);
        setPadding(new Insets(20, 15, 20, 15));
        setMinWidth(200);
        setMaxWidth(200);
        getStyleClass().add("nav-sidebar");

        buildNav(isPersonal);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(spacer, new ThemeToggleBar());
    }

    private void buildNav(boolean isPersonal) {
        Map<String, String> items = new LinkedHashMap<>();

        if (isPersonal) {
            items.put("🏠", "Home");
            items.put("📚", "Library");
            items.put("📝", "Notes");
            items.put("✨", "Recommendations");

            if (session.isAdmin()) {
                items.put("🛠", "Admin Actions");
            }
        } else {
            items.put("🏠", "Club Home");
            items.put("🗣", "Discussion");
            items.put("📝", "Notes");
            items.put("📅", "Meetings");

            ClubRole role = session.getHighestRole();
            switch (role) {
                case MODERATOR -> items.put("🛠︎", "Actions");
                case OWNER    -> items.put("⚙", "Actions");
            }
        }

        items.forEach(this::addNavButton);
    }

    private void addNavButton(String emoji, String text) {
        Button button = new Button(emoji + " " + text);
        button.getStyleClass().add("nav-button");
        button.setOnAction(e -> contentArea.showView(text));
        getChildren().add(button);
    }
}
