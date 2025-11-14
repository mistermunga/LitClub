package com.litclub.ui.crossroads.components;

import com.litclub.ui.crossroads.service.CrossRoadsService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Section for creating new clubs (with permission checks)
 */
public class CreateClubSection extends VBox {

    private final CrossRoadsService service;
    private Button createClubButton;

    public CreateClubSection(CrossRoadsService service, Runnable onCreateClick) {
        super(15);
        this.service = service;

        setAlignment(Pos.CENTER);
        setPadding(new Insets(20, 0, 20, 0));

        // Only show if user can create clubs
        if (!service.canCreateClubs()) {
            return;
        }

        // Check if user has reached club limit
        if (service.hasReachedClubLimit()) {
            Label limitLabel = new Label("You've reached the maximum number of clubs (" +
                    service.getMaxClubsPerUser() + ")");
            limitLabel.getStyleClass().add("text-muted");
            limitLabel.setStyle("-fx-font-style: italic;");
            getChildren().add(limitLabel);
            this.createClubButton = null;
            return;
        }

        createClubButton = new Button("+ Create New Club");
        createClubButton.getStyleClass().add("button-primary");
        createClubButton.setOnAction(e -> onCreateClick.run());

        // Show approval notice if needed
        if (service.clubsNeedApproval()) {
            Label approvalNotice = new Label("New clubs require administrator approval");
            approvalNotice.getStyleClass().add("text-muted");
            approvalNotice.setStyle("-fx-font-size: 12px; -fx-font-style: italic;");
            getChildren().addAll(createClubButton, approvalNotice);
        } else {
            getChildren().add(createClubButton);
        }
    }
}
