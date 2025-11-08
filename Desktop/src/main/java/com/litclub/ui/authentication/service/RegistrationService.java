package com.litclub.ui.authentication.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.interfaces.auth.AuthResponse;
import com.litclub.construct.interfaces.user.UserRegistrationRecord;
import com.litclub.persistence.repository.LibraryRepository;
import javafx.application.Platform;

import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Service layer for handling user registration.
 *
 * <p>Validates registration data and communicates with the repository
 * to create new user accounts. All callbacks execute on the JavaFX Application Thread.</p>
 */
public class RegistrationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{1,64}@)[\\p{L}\\p{N}._%+-]+@[\\p{L}\\p{N}.-]+\\.[\\p{L}]{2,}$",
            Pattern.UNICODE_CASE
    );

    private final UserRegistrationRecord userRegistrationRecord;

    public RegistrationService(
            String username,
            String firstName,
            String surname,
            String email,
            String password,
            String passwordConfirm
    ) {
        // Validate all inputs (throws IllegalArgumentException on failure)
        validateUsername(username);
        validateName(firstName, "First name");
        validateName(surname, "Surname");
        validateEmail(email);
        validatePassword(password);
        validatePasswordConfirmation(passwordConfirm);
        validatePasswordsMatch(password, passwordConfirm);

        // Create registration record (not admin by default)
        this.userRegistrationRecord = new UserRegistrationRecord(
                username,
                firstName,
                surname,
                email,
                password,
                false
        );
    }

    // ==================== VALIDATION METHODS ====================

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("Username cannot exceed 50 characters");
        }
        // Check for invalid characters
        if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, dots, hyphens, and underscores");
        }
    }

    private void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException(fieldName + " cannot exceed 100 characters");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password cannot exceed 128 characters");
        }
    }

    private void validatePasswordConfirmation(String passwordConfirm) {
        if (passwordConfirm == null || passwordConfirm.isEmpty()) {
            throw new IllegalArgumentException("Please confirm your password");
        }
    }

    private void validatePasswordsMatch(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    // ==================== REGISTRATION ====================

    /**
     * Performs registration with callbacks for success and failure.
     *
     * <p>This method is non-blocking - it returns immediately while registration
     * happens asynchronously in the background.</p>
     *
     * @param onSuccess callback when registration succeeds (receives AuthResponse)
     * @param onError callback when registration fails (receives user-friendly error message)
     */
    public void register(Consumer<AuthResponse> onSuccess, Consumer<String> onError) {
        LibraryRepository.getInstance().register(userRegistrationRecord)
                .thenAccept(authResponse -> {
                    // Registration successful
                    Platform.runLater(() -> {
                        onSuccess.accept(authResponse);
                    });
                })
                .exceptionally(throwable -> {
                    // Registration failed - use ApiErrorHandler for consistent error messages
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Alternative registration method that takes a Runnable for simpler success handling
     * when you don't need the AuthResponse.
     */
    public void register(Runnable onSuccess, Consumer<String> onError) {
        register(
                authResponse -> onSuccess.run(),
                onError
        );
    }
}