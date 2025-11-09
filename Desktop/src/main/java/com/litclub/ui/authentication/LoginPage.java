package com.litclub.ui.authentication;

import com.litclub.SceneManager;
import com.litclub.theme.ThemeManager;
import com.litclub.theme.ThemeToggleBar;
import com.litclub.ui.authentication.service.LoginService;
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
    private Button loginButton;
    private final Label statusLabel;

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
        identifierField.setPromptText("'mariedoe' or 'mariedoe@example.com'");
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

        loginButton = new Button("Login");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setDefaultButton(true);
        HBox.setHgrow(loginButton, Priority.ALWAYS);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        setupLoginEvent(loginButton);

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("secondary-button");
        HBox.setHgrow(registerButton, Priority.ALWAYS);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(e -> SceneManager.getInstance().showRegistration());

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

    private void setupLoginEvent(Button loginButton) {
        loginButton.setOnAction(e -> {
            String identifier = identifierField.getText().trim();
            String password = passwordField.getText();

            // Validate inputs
            if (identifier.isEmpty()) {
                showError("Identifier is missing");
                return;
            }

            if (password.isEmpty()) {
                showError("Password is missing");
                return;
            }

            // Attempt login
            try {
                // Show loading state
                setLoadingState(true);
                showInfo();

                LoginService loginService = new LoginService(identifier, password);

                // Perform async login with callbacks
                loginService.login(
                        // Success callback
                        authResponse -> {
                            setLoadingState(false);
                            showSuccess("Login successful! Welcome, " + authResponse.userRecord().username());

                            // Navigate to main app after short delay
                            // (so userRecord can see success message)
                            javafx.animation.PauseTransition pause =
                                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.5));
                            pause.setOnFinished(event ->
                                    SceneManager.getInstance().showCrossRoads()
                            );
                            pause.play();
                        },
                        // Error callback
                        errorMessage -> {
                            setLoadingState(false);
                            showError(errorMessage);
                        }
                );

            } catch (IllegalArgumentException exception) {
                setLoadingState(false);
                showError(exception.getMessage());
            }
        });
    }

    /**
     * Enables/disables UI during login attempt
     */
    private void setLoadingState(boolean isLoading) {
        loginButton.setDisable(isLoading);
        identifierField.setDisable(isLoading);
        passwordField.setDisable(isLoading);
    }

    /**
     * Shows an error message to the userRecord
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().removeAll("info-label", "success-label");
        statusLabel.getStyleClass().add("error-label");
    }

    /**
     * Shows an info message to the userRecord
     */
    private void showInfo() {
        statusLabel.setText("Logging in...");
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        statusLabel.getStyleClass().add("info-label");
    }

    /**
     * Shows a success message to the userRecord
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().removeAll("error-label", "info-label");
        statusLabel.getStyleClass().add("success-label");
    }
}