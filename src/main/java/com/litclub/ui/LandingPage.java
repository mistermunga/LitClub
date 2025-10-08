package com.litclub.ui;

import com.litclub.MainApplication;
import com.litclub.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class LandingPage extends VBox {

    public LandingPage() {

        ThemeManager.getInstance().registerComponent(this);

        this.getStyleClass().add("landing-root");
        this.setAlignment(Pos.CENTER);
        this.setSpacing(40);
        this.setPadding(new Insets(60, 40, 60, 40));

        // Components
        ImageView logo = createLogoHeader();
        VBox instanceSelector = createInstanceSelector();

        // Main layout
        this.getChildren().addAll(logo, instanceSelector);
    }

    private ImageView createLogoHeader() {
        String imagePath = ThemeManager.getInstance().isBrightMode()
                ? "/com/litclub/ui/icons/LitClub_logo_blackText.png"
                : "/com/litclub/ui/icons/LitClub_logo_whiteText.png";

        Image image = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(imagePath),
                "Logo image not found at: " + imagePath
        ));

        ImageView logo = new ImageView(image);
        logo.setPreserveRatio(true);
        logo.setFitWidth(350);
        logo.getStyleClass().add("landing-logo");

        // Add listener to update logo when theme changes
        ThemeManager.getInstance().brightModeProperty().addListener(
                (observable, oldValue, newValue) -> {
                    String newImagePath = newValue
                            ? "/com/litclub/ui/icons/LitClub_logo_blackText.png"
                            : "/com/litclub/ui/icons/LitClub_logo_whiteText.png";

                    Image newImage = new Image(Objects.requireNonNull(
                            getClass().getResourceAsStream(newImagePath),
                            "Logo image not found at: " + newImagePath
                    ));

                    logo.setImage(newImage);
                }
        );

        return logo;
    }

    private VBox createInstanceSelector() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(20);
        container.getStyleClass().add("instance-container");

        // Label
        Label urlLabel = new Label("Enter Instance URL");
        urlLabel.getStyleClass().add("instance-label");

        // Input + Go Button Row
        HBox inputRow = new HBox();
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setSpacing(10);

        TextField instanceURL = new TextField();
        instanceURL.setPromptText("abulafia.litclub.com");
        instanceURL.getStyleClass().add("instance-input");
        HBox.setHgrow(instanceURL, Priority.ALWAYS);
        instanceURL.setOnAction(e -> MainApplication.getInstance().showLogin());

        Button goButton = new Button("Go");
        goButton.getStyleClass().add("instance-button");
        goButton.setDefaultButton(true);
        goButton.setOnAction(e -> MainApplication.getInstance().showLogin());

        inputRow.getChildren().addAll(instanceURL, goButton);

        // Theme toggle button
        Button toggleButton = new Button();
        toggleButton.getStyleClass().add("toggle-button");
        updateToggleButtonText(ThemeManager.getInstance().isBrightMode(), toggleButton);

        // Update button text on theme change
        ThemeManager.getInstance().brightModeProperty().addListener(
                (observable, oldValue, newValue) -> updateToggleButtonText(newValue, toggleButton)
        );
        toggleButton.setOnAction(event -> ThemeManager.getInstance().toggleTheme());

        // Sub-container for visual breathing space
        VBox toggleContainer = new VBox(toggleButton);
        toggleContainer.setAlignment(Pos.CENTER);
        toggleContainer.setPadding(new Insets(20, 0, 0, 0));

        container.getChildren().addAll(urlLabel, inputRow, toggleContainer);

        return container;
    }

    private void updateToggleButtonText(boolean isBrightMode, Button button) {
        button.setText(isBrightMode ? "Switch to Dark Mode" : "Switch to Light Mode");
    }
}
