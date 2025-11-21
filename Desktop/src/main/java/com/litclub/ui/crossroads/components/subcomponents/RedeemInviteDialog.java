package com.litclub.ui.crossroads.components.subcomponents;

import com.litclub.ui.crossroads.service.CrossRoadsService;
import com.litclub.ui.main.shared.view.subcomponent.common.BaseAsyncDialog;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Dialog for redeeming club invitation codes.
 */
public class RedeemInviteDialog extends BaseAsyncDialog<Void> {

    private final CrossRoadsService crossRoadsService;
    private TextField inviteField;

    public RedeemInviteDialog(CrossRoadsService crossRoadsService) {
        super("Redeem Invite", "Redeem");
        this.crossRoadsService = crossRoadsService;
        setHeaderText("Enter your invitation code to join a club");
        initializeUI();
    }

    @Override
    protected Node createFormContent() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));

        Label titleLabel = new Label("Invite Code:");
        titleLabel.getStyleClass().add("label");

        inviteField = new TextField();
        inviteField.setPromptText("Paste your invite code here");
        inviteField.getStyleClass().add("text-input");

        grid.add(titleLabel, 0, 0);
        grid.add(inviteField, 1, 0);

        return grid;
    }

    @Override
    protected void setupFormValidation() {
        inviteField.textProperty().addListener((obs, old, val) -> updateSubmitButtonState());
    }

    @Override
    protected boolean isFormValid() {
        return inviteField != null && !inviteField.getText().trim().isEmpty();
    }

    @Override
    protected boolean validateForm() {
        String code = inviteField.getText().trim();
        if (code.isEmpty()) {
            showError("Please enter an invite code");
            return false;
        }
        return true;
    }

    @Override
    protected void handleAsyncSubmit() {
        String code = inviteField.getText().trim();
        System.out.println("Redeeming invite: " + code);

        crossRoadsService.redeemInvite(
                code,
                // Success: explicitly call onSubmitSuccess and then close dialog on FX thread
                (() -> Platform.runLater(() -> {
                    onSubmitSuccess(null);
                    close();
                })),
                // Error
                this::onSubmitError
        );
    }

    @Override
    protected String getSuccessMessage(Void result) {
        return "Invite redeemed!";
    }

    @Override
    protected double getSuccessCloseDelay() {
        return 1.5;
    }
}
