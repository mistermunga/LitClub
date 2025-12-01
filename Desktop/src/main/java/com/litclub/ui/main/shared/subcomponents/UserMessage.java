package com.litclub.ui.main.shared.subcomponents;

import com.litclub.SceneManager;
import com.litclub.session.AppSession;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class UserMessage extends HBox {

    public UserMessage() {
        this.setAlignment(Pos.CENTER_RIGHT);
        this.setSpacing(5);

        Label greeting = new Label("Hello,");
        greeting.getStyleClass().add("greeting-label");

        Button userButton = new Button(AppSession.getInstance().getUserRecord().username());
        userButton.getStyleClass().add("user-button");

        userButton.setOnAction(e -> {
            System.out.println("Usercard clicked! Event: " + e);
            System.out.println("Component still in scene: " + (getScene() != null));
            AppSession.getInstance().clearClubContext();
            SceneManager.getInstance().showMainPage(true);
        });

        this.getChildren().addAll(greeting, userButton);
    }
}
