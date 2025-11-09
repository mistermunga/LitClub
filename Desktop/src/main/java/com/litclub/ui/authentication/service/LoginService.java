package com.litclub.ui.authentication.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.interfaces.auth.AuthResponse;
import com.litclub.construct.interfaces.user.UserLoginRecord;
import com.litclub.persistence.repository.LibraryRepository;
import com.litclub.session.AppSession;
import javafx.application.Platform;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Service layer for handling user authentication (login/register).
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
     * @param onError callback when login fails (receives user-friendly error message)
     */
    public void login(Consumer<AuthResponse> onSuccess, Consumer<String> onError) {
        LibraryRepository.getInstance().login(userLoginRecord)
                .thenAccept(authResponse -> {
                    // Login successful
                    AppSession.getInstance().setUserRecord(authResponse.userRecord());
                    // Ensure UI updates happen on JavaFX Application Thread
                    Platform.runLater(() -> {
                        onSuccess.accept(authResponse);
                    });
                })
                .exceptionally(throwable -> {
                    // Login failed - use ApiErrorHandler for consistent error messages
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept(errorMessage);
                    });
                    return null;
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
     * Validates if a string is a valid email address.
     */
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}