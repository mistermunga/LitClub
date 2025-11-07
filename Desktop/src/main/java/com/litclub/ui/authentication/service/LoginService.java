package com.litclub.ui.authentication.service;

import com.litclub.client.api.ApiClient;
import com.litclub.construct.interfaces.auth.AuthResponse;
import com.litclub.construct.interfaces.user.UserLoginRecord;
import com.litclub.persistence.repository.LibraryRepository;
import javafx.application.Platform;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Service layer for handling userRecord authentication (login/register).
 *
 * <p>This service handles async operations and provides callbacks for UI updates.
 * All callbacks are automatically executed on the JavaFX Application Thread.</p>
 */
public class LoginService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{1,64}@)[\\p{L}\\p{N}._%+-]+@[\\p{L}\\p{N}.-]+\\.[\\p{L}]{2,}$",
            Pattern.UNICODE_CASE
    );

    private final UserLoginRecord userLoginRecord;

    public LoginService(String identifier, String password) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if (isValidEmail(identifier)) {
            // identifier is an email
            userLoginRecord = new UserLoginRecord(Optional.of(identifier), Optional.empty(), password);
        } else {
            // identifier is a username
            userLoginRecord = new UserLoginRecord(Optional.empty(), Optional.of(identifier), password);
        }
    }

    /**
     * Performs login with callbacks for success and failure.
     *
     * <p>This method is non-blocking - it returns immediately while the login
     * happens asynchronously in the background.</p>
     *
     * @param onSuccess callback when login succeeds (receives AuthResponse)
     * @param onError callback when login fails (receives userRecord-friendly error message)
     */
    public void login(Consumer<AuthResponse> onSuccess, Consumer<String> onError) {
        LibraryRepository.getInstance().login(userLoginRecord)
                .thenAccept(authResponse -> {
                    // Login successful
                    // Ensure UI updates happen on JavaFX Application Thread
                    Platform.runLater(() -> {
                        onSuccess.accept(authResponse);
                    });
                })
                .exceptionally(throwable -> {
                    // Login failed
                    Platform.runLater(() -> {
                        String errorMessage = parseErrorMessage(throwable);
                        onError.accept(errorMessage);
                    });
                    return null; // Required by exceptionally
                });
    }

    /**
     * Alternative login method that takes a Runnable for simpler success handling
     * when you don't need the AuthResponse.
     */
    public void login(Runnable onSuccess, Consumer<String> onError) {
        login(
                authResponse -> onSuccess.run(),
                onError
        );
    }

    /**
     * Parses throwables from the API client into userRecord-friendly error messages.
     *
     * @param throwable the exception thrown during login
     * @return userRecord-friendly error message
     */
    private String parseErrorMessage(Throwable throwable) {
        Throwable actualError = throwable;
        while (actualError.getCause() != null &&
                actualError.getClass().getName().contains("CompletionException")) {
            actualError = actualError.getCause();
        }

        String message = actualError.getMessage();

        // Check if it's an ApiException
        if (actualError instanceof ApiClient.ApiException apiException) {
            String responseBody = apiException.getResponseBody();

            // Parse response body for more specific errors if available
            if (responseBody != null && !responseBody.isEmpty()) {
                // You might want to parse JSON here for specific error messages
                // For now, we'll use the status code from the message
            }
        }

        // Handle specific HTTP status codes
        if (message != null) {
            if (message.contains("401")) {
                return "Invalid username/email or password. Please try again.";
            } else if (message.contains("403")) {
                return "Access denied. Your account may be locked.";
            } else if (message.contains("404")) {
                return "User not found. Please check your credentials.";
            } else if (message.contains("500")) {
                return "Server error. Please try again later.";
            } else if (message.contains("timeout") || message.contains("timed out")) {
                return "Connection timeout. Please check your internet connection.";
            } else if (message.contains("Connection refused") ||
                    message.contains("Unable to connect")) {
                return "Unable to connect to server. Please check your network.";
            } else if (message.contains("Failed to serialize")) {
                return "Invalid request format. Please try again.";
            } else if (message.contains("Failed to deserialize")) {
                return "Invalid response from server. Please contact support.";
            }
        }

        // Generic fallback message
        return "Login failed: " + (message != null ? message : "Unknown error");
    }

    /**
     * Validates if a string is a valid email address.
     */
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}