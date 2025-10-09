package com.litclub.ui.component.subcomponent;

import com.litclub.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ThemeToggleBar extends VBox {

    public ThemeToggleBar() {
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(20, 0, 0, 0));

        showToggleButton();
    }

    private void showToggleButton() {
        // Theme toggle button
        Button toggleButton = new Button();
        toggleButton.getStyleClass().add("theme-toggle");
        updateToggleButtonText(ThemeManager.getInstance().isBrightMode(), toggleButton);

        // Update button text on theme change
        ThemeManager.getInstance().brightModeProperty().addListener(
                (observable, oldValue, newValue) -> updateToggleButtonText(newValue, toggleButton)
        );
        toggleButton.setOnAction(event -> ThemeManager.getInstance().toggleTheme());

        this.getChildren().add(toggleButton);
    }

    private void updateToggleButtonText(boolean isBrightMode, Button button) {
        button.setText(isBrightMode ? "Switch to Dark Mode" : "Switch to Light Mode");
    }
}
