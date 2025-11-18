package com.litclub.ui.crossroads.components.subcomponents;

import com.litclub.SceneManager;
import com.litclub.session.AppSession;
import com.litclub.ui.crossroads.service.CrossRoadsService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class RedeemInviteDialog extends Dialog<String> {

    private Button redeemButton;
    private Button cancelButton;
    private TextField inviteField;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;

    private final CrossRoadsService crossRoadsService;
    private boolean isSubmitting;

    public RedeemInviteDialog(CrossRoadsService crossRoadsService) {
        this.crossRoadsService = crossRoadsService;

        setTitle("Redeem Invite");
        setHeaderText("Enter invitation code");
        setResizable(true);

        ButtonType redeemButtonType = new ButtonType("Redeem", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(redeemButtonType, ButtonType.CANCEL);

        // Build UI
        VBox content = createContent();
        getDialogPane().setContent(content);

        // Get references to buttons
        redeemButton = (Button) getDialogPane().lookupButton(redeemButtonType);
        cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        redeemButton.setDisable(true);

        // Enable redeem button only when invite field has text
        inviteField.textProperty().addListener((obs, old, val) ->
                redeemButton.setDisable(val.trim().isEmpty() || isSubmitting)
        );

        // Prevent dialog from closing on button click
        redeemButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!isSubmitting) {
                event.consume(); // Prevent default close behavior
                handleSubmit();
            }
        });

        // Don't use result converter - handle submission manually
        setResultConverter(buttonType -> null);
    }

    private VBox createContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setMinWidth(500);
        container.getStyleClass().add("container");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        Label titleLabel = new Label("Invite Code:");
        titleLabel.getStyleClass().add("label");

        inviteField = new TextField();
        inviteField.setPromptText("Paste your invite code here");
        inviteField.getStyleClass().add("text-input");

        grid.add(titleLabel, 0, 0);
        grid.add(inviteField, 1, 0);

        // Status message
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(450);

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(30, 30);
        loadingIndicator.setVisible(false);

        container.getChildren().addAll(grid, statusLabel, loadingIndicator);
        return container;
    }

    private void handleSubmit() {
        if (isSubmitting) return;

        String inviteCode = inviteField.getText().trim();

        // Basic validation
        if (inviteCode.isEmpty()) {
            showError("Please enter an invite code");
            return;
        }

        isSubmitting = true;
        redeemButton.setDisable(true);
        cancelButton.setDisable(true);

        submitCode(inviteCode);
    }

    private void submitCode(String code) {
        loadingIndicator.setVisible(true);
        statusLabel.setVisible(false);

        System.out.println("Redeeming invite: " + code);

        crossRoadsService.redeemInvite(
                code,
                // Success callback
                club -> Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showSuccess("Successfully joined " + club.getClubName() + "!");

                    // Close dialog after delay
                    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                    pause.setOnFinished(e -> {
                        setResult(code);
                        AppSession.getInstance().setCurrentClub(club);
                        SceneManager.getInstance().showMainPage(false);
                        close();
                    });
                    pause.play();
                }),
                // Error callback
                errorMessage -> Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    resetSubmittingState();
                    showError(errorMessage);
                })
        );
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error-label");
        statusLabel.getStyleClass().add("success-label");
        statusLabel.setVisible(true);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("success-label");
        statusLabel.getStyleClass().add("error-label");
        statusLabel.setVisible(true);
    }

    private void resetSubmittingState() {
        isSubmitting = false;
        redeemButton.setDisable(false);
        cancelButton.setDisable(false);
    }
}