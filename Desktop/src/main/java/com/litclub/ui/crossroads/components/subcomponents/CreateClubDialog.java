package com.litclub.ui.crossroads.components.subcomponents;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Dialog for creating a new club
 */
public class CreateClubDialog extends Dialog<ClubData> {

    public CreateClubDialog() {

        setTitle("Create New Club");
        setHeaderText("Enter club details");

        ButtonType createBtnType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        // UI controls
        TextField nameField = new TextField();
        nameField.setPromptText("Club name");

        TextArea descField = new TextArea();
        descField.setPromptText("Description (optional)");
        descField.setWrapText(true);

        Label charCount = new Label("0/500");
        descField.textProperty().addListener((obs, old, v) -> {
            if (v.length() > 500) descField.setText(old);
            charCount.setText(descField.getText().length() + "/500");
        });

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Club Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(charCount, 1, 2);

        getDialogPane().setContent(grid);

        // Disable Create button until valid
        Node createButton = getDialogPane().lookupButton(createBtnType);
        createButton.setDisable(true);

        nameField.textProperty().addListener((obs, old, v) -> {
            createButton.setDisable(v.trim().isEmpty());
        });

        // Convert to result
        setResultConverter(btn -> {
            if (btn == createBtnType) {
                return new ClubData(
                        nameField.getText().trim(),
                        descField.getText().trim()
                );
            }
            return null;
        });
    }
}


