package com.litclub.ui.main.shared;

import com.litclub.SceneManager;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.subcomponents.MiniLogo;
import com.litclub.ui.main.shared.subcomponents.UserMessage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HeaderBar extends HBox {

    public HeaderBar() {
        ThemeManager.getInstance().registerComponent(this);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(10, 30, 10, 30));
        this.setSpacing(12);
        this.setMaxHeight(60);
        this.getStyleClass().add("header-bar");

        addLogoSection();
        addSpacer();
        addGreeting();
    }

    private void addLogoSection() {
        HBox logoSection = new HBox(10);
        logoSection.setAlignment(Pos.BASELINE_CENTER);

        MiniLogo miniLogo = new MiniLogo();
        miniLogo.setOnMouseClicked(e -> {
            AppSession.getInstance().clearClubContext();
            SceneManager.getInstance().showCrossRoads();
        });

        logoSection.getChildren().addAll(miniLogo);
        this.getChildren().add(logoSection);
    }

    private void addSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        this.getChildren().add(spacer);
    }

    private void addGreeting() {
        UserMessage userMessage = new UserMessage();
        userMessage.setAlignment(Pos.BASELINE_CENTER);
        this.getChildren().add(userMessage);
    }
}
