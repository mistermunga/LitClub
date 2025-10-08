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
        String imagePath = "/com/litclub/ui/icons/LitClub_logo_blackText.png";

        Image image = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(imagePath),
                "Logo image not found at: " + imagePath
        ));

        ImageView logo = new ImageView(image);
        logo.setPreserveRatio(true);
        logo.setFitWidth(350);
        logo.getStyleClass().add("landing-logo");

        return logo;
    }

    private VBox createInstanceSelector() {
        VBox container = new VBox();

        container.setAlignment(Pos.CENTER);
        container.setSpacing(15);
        container.getStyleClass().add("instance-container");

        Label urlLabel = new Label("Enter Instance URL");
        urlLabel.getStyleClass().add("instance-label");

        HBox inputRow = new HBox();
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setSpacing(10);

        TextField instanceURL = new TextField();
        instanceURL.setPromptText("abulafia.litclub.com");
        instanceURL.getStyleClass().add("instance-input");
        HBox.setHgrow(instanceURL, Priority.ALWAYS);
        instanceURL.setOnAction(e -> {MainApplication.getInstance().showLogin();});

        Button goButton = new Button("Go");
        goButton.getStyleClass().add("instance-button");
        goButton.setDefaultButton(true);
        goButton.setOnAction(e -> MainApplication.getInstance().showLogin());

        inputRow.getChildren().addAll(instanceURL, goButton);
        container.getChildren().addAll(urlLabel, inputRow);

        return container;
    }
}
