package com.litclub.ui.component.subunits;

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

        Button userButton = new Button(AppSession.getInstance().getUserRecord().firstname());
        userButton.getStyleClass().add("user-button");

        this.getChildren().addAll(greeting, userButton);
    }
}