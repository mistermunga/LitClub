package com.litclub.ui.crossroads.components.subcomponents;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class InviteCard extends VBox {

    public InviteCard(Runnable onRedeem) {
        super(15);
        getStyleClass().add("invite-card");

        // + icon
        Label plus = new Label("+");
        plus.getStyleClass().add("invite-plus");

        // Title
        Label title = new Label("Redeem Invite");
        title.getStyleClass().add("invite-title");

        getChildren().addAll(plus, title);

        setOnMouseClicked(e -> onRedeem.run());
    }
}

