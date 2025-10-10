package com.litclub.ui;

import com.litclub.SceneManager;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.subcomponent.ThemeToggleBar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LoginPage extends VBox {

    private TextField identifierField;
    private PasswordField passwordField;
    private Label statusLabel;

    public LoginPage() {
        ThemeManager.getInstance().registerComponent(this);

        this.getStyleClass().add("root");
        this.setAlignment(Pos.CENTER);
        this.setSpacing(40);
        this.setPadding(new Insets(60, 40, 60, 40));

        statusLabel = new Label();
        statusLabel.setVisible(false);

        showLoginCard();
        showButtons();
    }

    private void showLoginCard() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(20);
        container.getStyleClass().add("container");

        Label titleLabel = new Label("Sign In");
        titleLabel.getStyleClass().add("title");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setPadding(new Insets(20));

        Label identifierLabel = new Label("Username or Email");
        identifierLabel.getStyleClass().add("label");
        identifierField = new TextField();
        identifierField.setPromptText("'Marie Doe' or 'Mariedoe@example.com'");
        identifierField.getStyleClass().add("text-input");

        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("label");
        passwordField = new PasswordField();
        passwordField.getStyleClass().add("text-input");

        statusLabel.getStyleClass().add("status-label");

        formGrid.add(identifierLabel, 0, 0);
        formGrid.add(identifierField, 1, 0);
        formGrid.add(passwordLabel, 0, 1);
        formGrid.add(passwordField, 1, 1);
        formGrid.add(statusLabel, 0, 2, 2, 1); // spans 2 columns

        container.getChildren().addAll(titleLabel, formGrid);

        this.getChildren().add(container);
    }

    private void showButtons() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(20);

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(10);

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setDefaultButton(true);
        HBox.setHgrow(loginButton, Priority.ALWAYS);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        setupLoginEvent(loginButton);

        Button registerButton = new Button("Register"); // TODO add the registration page
        registerButton.getStyleClass().add("secondary-button");
        HBox.setHgrow(registerButton, Priority.ALWAYS);
        registerButton.setMaxWidth(Double.MAX_VALUE);


        Button backButton = new Button("Back");
        backButton.getStyleClass().add("secondary-button");
        HBox.setHgrow(backButton, Priority.ALWAYS);
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(e -> SceneManager.getInstance().showLanding());

        buttonContainer.getChildren().addAll(loginButton, registerButton, backButton);

        VBox toggleContainer = new ThemeToggleBar();

        container.getChildren().addAll(buttonContainer, toggleContainer);
        this.getChildren().add(container);
    }

    // TODO CRITICAL ADD PROPER LOGIC
    private void setupLoginEvent(Button loginButton) {
        loginButton.setOnAction(e -> {
            String identifier = identifierField.getText().trim();
            String password = passwordField.getText().trim();

            if ((identifier.equals("Marie Doe") || identifier.equals("Mariedoe@example.com")) && password.equals("Example")) {
                // TODO setting details in session -> This should be an API CALL
                AppSession.getInstance().setCredentials(
                        "Marie",
                        "Doe",
                        "Mariedoe",
                        "Mariedoe@example.com");
                // TODO Pass details about what is to be displayed
                SceneManager.getInstance().showCrossRoads();
            } else {
                showAccessDeniedError(identifier, password);
            }
        });
    }

    // TODO Add more specific error status displays
    private void showAccessDeniedError(String identifier, String password) {
        statusLabel.setText("Access denied. Please check your credentials.");
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().add("error-label");
    }
}