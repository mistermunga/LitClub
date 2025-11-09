package com.litclub.ui.authentication;

import com.litclub.SceneManager;
import com.litclub.theme.ThemeManager;
import com.litclub.theme.ThemeToggleBar;
import com.litclub.ui.authentication.service.RegistrationService;
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

public class RegistrationPage extends VBox {

    private TextField usernameField;
    private TextField firstNameField;
    private TextField surnameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button registerButton;
    private Button backButton;
    private final Label statusLabel;

    public RegistrationPage() {
        ThemeManager.getInstance().registerComponent(this);

        this.getStyleClass().add("root");
        this.setAlignment(Pos.CENTER);
        this.setSpacing(40);
        this.setPadding(new Insets(60, 40, 60, 40));

        statusLabel = new Label();
        statusLabel.setVisible(false);

        showRegistrationCard();
        showButtons();
    }

    private void showRegistrationCard() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(20);
        container.getStyleClass().add("container");

        Label titleLabel = new Label("Create Account");
        titleLabel.getStyleClass().add("title");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setPadding(new Insets(20));

        // Username
        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("label");
        usernameField = new TextField();
        usernameField.setPromptText("mariedoe");
        usernameField.getStyleClass().add("text-input");

        // First Name
        Label firstNameLabel = new Label("First Name");
        firstNameLabel.getStyleClass().add("label");
        firstNameField = new TextField();
        firstNameField.setPromptText("Marie");
        firstNameField.getStyleClass().add("text-input");

        // Surname
        Label surnameLabel = new Label("Surname");
        surnameLabel.getStyleClass().add("label");
        surnameField = new TextField();
        surnameField.setPromptText("Doe");
        surnameField.getStyleClass().add("text-input");

        // Email
        Label emailLabel = new Label("Email");
        emailLabel.getStyleClass().add("label");
        emailField = new TextField();
        emailField.setPromptText("mariedoe@example.com");
        emailField.getStyleClass().add("text-input");

        // Password
        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("label");
        passwordField = new PasswordField();
        passwordField.setPromptText("Min. 8 characters");
        passwordField.getStyleClass().add("text-input");

        // Confirm Password
        Label confirmPasswordLabel = new Label("Confirm Password");
        confirmPasswordLabel.getStyleClass().add("label");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter password");
        confirmPasswordField.getStyleClass().add("text-input");

        // Status Label
        statusLabel.getStyleClass().add("status-label");

        // Add all fields to grid
        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);
        formGrid.add(firstNameLabel, 0, 1);
        formGrid.add(firstNameField, 1, 1);
        formGrid.add(surnameLabel, 0, 2);
        formGrid.add(surnameField, 1, 2);
        formGrid.add(emailLabel, 0, 3);
        formGrid.add(emailField, 1, 3);
        formGrid.add(passwordLabel, 0, 4);
        formGrid.add(passwordField, 1, 4);
        formGrid.add(confirmPasswordLabel, 0, 5);
        formGrid.add(confirmPasswordField, 1, 5);
        formGrid.add(statusLabel, 0, 6, 2, 1); // spans 2 columns

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

        registerButton = new Button("Register");
        registerButton.getStyleClass().add("button-primary");
        registerButton.setDefaultButton(true);
        HBox.setHgrow(registerButton, Priority.ALWAYS);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        setupRegistrationEvent(registerButton);

        backButton = new Button("Back");
        backButton.getStyleClass().add("secondary-button");
        HBox.setHgrow(backButton, Priority.ALWAYS);
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(e -> SceneManager.getInstance().showLogin());

        buttonContainer.getChildren().addAll(backButton, registerButton);

        VBox toggleContainer = new ThemeToggleBar();

        container.getChildren().addAll(buttonContainer, toggleContainer);
        this.getChildren().add(container);
    }

    private void setupRegistrationEvent(Button registerButton) {
        registerButton.setOnAction(e -> {
            // Trim all inputs
            String username = usernameField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String surname = surnameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Basic field presence check (detailed validation happens in service)
            if (username.isEmpty() || firstName.isEmpty() || surname.isEmpty() ||
                    email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showError("All fields are required");
                return;
            }

            try {
                // Show loading state
                setLoadingState(true);
                showInfo("Creating your account...");

                // Create registration service (this validates inputs)
                RegistrationService registrationService = new RegistrationService(
                        username,
                        firstName,
                        surname,
                        email,
                        password,
                        confirmPassword
                );

                // Perform async registration with callbacks
                registrationService.register(
                        // Success callback
                        authResponse -> {
                            setLoadingState(false);
                            showSuccess("Registration successful! Welcome, " + authResponse.userRecord().username());

                            // Navigate to main app after short delay
                            javafx.animation.PauseTransition pause =
                                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
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
                // Validation errors from RegistrationService constructor
                setLoadingState(false);
                showError(exception.getMessage());
            } catch (Exception exception) {
                setLoadingState(false);
                showError("An unexpected error occurred. Please try again.");
            }
        });
    }

    /**
     * Enables/disables UI during registration attempt
     */
    private void setLoadingState(boolean isLoading) {
        registerButton.setDisable(isLoading);
        backButton.setDisable(isLoading);
        usernameField.setDisable(isLoading);
        firstNameField.setDisable(isLoading);
        surnameField.setDisable(isLoading);
        emailField.setDisable(isLoading);
        passwordField.setDisable(isLoading);
        confirmPasswordField.setDisable(isLoading);
    }

    /**
     * Shows an error message to the user
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().removeAll("info-label", "success-label");
        statusLabel.getStyleClass().add("error-label");
    }

    /**
     * Shows an info message to the user
     */
    private void showInfo(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        statusLabel.getStyleClass().add("info-label");
    }

    /**
     * Shows a success message to the user
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().removeAll("error-label", "info-label");
        statusLabel.getStyleClass().add("success-label");
    }
}