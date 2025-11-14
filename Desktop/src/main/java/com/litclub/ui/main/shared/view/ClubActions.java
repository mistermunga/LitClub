package com.litclub.ui.main.shared.view;

import com.litclub.construct.enums.ClubRole;
import com.litclub.session.AppSession;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ClubActions extends VBox {

    public ClubActions() {
        this.setSpacing(12);
        this.getStyleClass().add("club-actions");

        ClubRole highestRole = AppSession.getInstance().getHighestRole();

        switch (highestRole) {
            case MODERATOR -> initModeratorUI();
            case OWNER -> initOwnerUI();
            default -> initDefaultUI();
        }
    }

    private void initModeratorUI() {
        getChildren().add(new Label("Moderator Tools"));
        // TODO add moderator-specific buttons, etc
    }

    private void initOwnerUI() {
        getChildren().add(new Label("Owner Tools"));
        // TODO add owner-specific controls
    }

    private void initDefaultUI() {
        getChildren().add(new Label("No actions available"));
    }
}
