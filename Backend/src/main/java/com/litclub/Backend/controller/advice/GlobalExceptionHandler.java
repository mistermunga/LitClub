package com.litclub.Backend.controller.advice;

import com.litclub.Backend.exception.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the LitClub application.
 *
 * <p>Provides centralized exception handling across all controllers, returning
 * consistent error responses to clients while logging detailed information for debugging.</p>
 *
 * <p><strong>Design Principles:</strong></p>
 * <ul>
 *   <li>Client responses are sanitized to prevent information leakage</li>
 *   <li>Stack traces are filtered to show only application code</li>
 *   <li>Full stack traces are logged to console for debugging</li>
 *   <li>HTTP status codes follow REST conventions</li>
 * </ul>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PACKAGE_PREFIX = "com.litclub.Backend";

    // ====== RESPONSE BUILDER ======

    /**
     * Builds a standardized error response.
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String safeMessage) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", safeMessage);
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Logs exception with filtered stack trace showing only application code.
     */
    private void logException(String level, String message, Throwable ex) {
        String filteredTrace = getFilteredStackTrace(ex);

        switch (level.toLowerCase()) {
            case "error" -> logger.error("{}\n{}", message, filteredTrace);
            case "warn" -> logger.warn("{}\n{}", message, filteredTrace);
            default -> logger.info("{}\n{}", message, filteredTrace);
        }
    }

    /**
     * Filters stack trace to show only application code with minimal framework noise.
     */
    private String getFilteredStackTrace(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");

        StackTraceElement[] trace = ex.getStackTrace();
        boolean foundAppCode = false;

        for (StackTraceElement element : trace) {
            String className = element.getClassName();

            // Show application code
            if (className.startsWith(PACKAGE_PREFIX)) {
                sb.append("  at ").append(element).append("\n");
                foundAppCode = true;
            }
            // Stop after we've seen app code and hit framework again
            else if (foundAppCode) {
                sb.append("  ... (framework code omitted)\n");
                break;
            }
        }

        // If there's a cause, show it too
        if (ex.getCause() != null && ex.getCause() != ex) {
            sb.append("Caused by: ").append(getFilteredStackTrace(ex.getCause()));
        }

        return sb.toString();
    }

    // ====== CUSTOM APPLICATION EXCEPTIONS ======

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        logException("warn", "User not found: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, "User not found");
    }

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleBookNotFound(BookNotFoundException ex) {
        logException("warn", "Book not found: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, "Book not found");
    }

    @ExceptionHandler(ClubNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleClubNotFound(ClubNotFoundException ex) {
        logException("warn", "Club not found: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, "Club not found");
    }

    @ExceptionHandler(MembershipNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleMembershipNotFound(MembershipNotFoundException ex) {
        logException("warn", "Membership not found: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, "Membership not found");
    }

    @ExceptionHandler(MissingLibraryItemException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleMissingLibraryItem(MissingLibraryItemException ex) {
        logException("warn", "Library item not found: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, "Book not found in user's library");
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleBookAlreadyExists(BookAlreadyExistsException ex) {
        logException("warn", "Book already exists: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, "Book already exists");
    }

    @ExceptionHandler(CredentialsAlreadyTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleCredentialsTaken(CredentialsAlreadyTakenException ex) {
        logException("warn", "Credentials already taken: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, "Username or email already in use");
    }

    @ExceptionHandler(MalformedDTOException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleMalformedDTO(MalformedDTOException ex) {
        logException("warn", "Malformed DTO: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request data: " + ex.getMessage());
    }

    @ExceptionHandler(MissingIdentifierException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleMissingIdentifier(MissingIdentifierException ex) {
        logException("warn", "Missing identifier: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Required identifier missing: " + ex.getMessage());
    }

    @ExceptionHandler(InsufficientPermissionsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Map<String, Object>> handleInsufficientPermissions(InsufficientPermissionsException ex) {
        logException("warn", "Insufficient permissions: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.FORBIDDEN, "Insufficient permissions to perform this action");
    }

    // ====== JPA / PERSISTENCE EXCEPTIONS ======

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        logException("warn", "Entity not found: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, "Requested resource not found");
    }

    @ExceptionHandler(EntityExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleEntityExists(EntityExistsException ex) {
        logException("warn", "Entity already exists: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, "Resource already exists");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleOptimisticLock(OptimisticLockingFailureException ex) {
        logException("warn", "Optimistic lock failure: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, "Resource was modified by another user. Please refresh and try again");
    }

    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockJPA(OptimisticLockException ex) {
        logException("warn", "JPA optimistic lock: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, "Resource was modified by another user. Please refresh and try again");
    }

    // ====== SPRING SECURITY EXCEPTIONS ======

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        logException("warn", "Bad credentials: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, Object>> handleInsufficientAuth(InsufficientAuthenticationException ex) {
        logException("warn", "Insufficient authentication: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        logException("warn", "Access denied: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        logException("warn", "Authentication failed: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed");
    }

    // ====== SPRING WEB / VALIDATION EXCEPTIONS ======

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        logException("warn", "Validation failed: " + ex.getMessage(), ex);

        // Extract field errors for more helpful message
        StringBuilder errors = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );

        String message = !errors.isEmpty() ? errors.toString() : "Validation failed";
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        logException("warn", "Malformed JSON: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        logException("warn", "Missing parameter: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Missing required parameter: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        logException("warn", "Type mismatch: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Invalid parameter type for: " + ex.getName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        logException("warn", "Method not supported: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method not supported: " + ex.getMethod());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleNoHandler(NoHandlerFoundException ex) {
        logException("warn", "No handler found: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, "Endpoint not found");
    }

    // ====== DATABASE / TRANSACTION EXCEPTIONS ======

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logException("warn", "Data integrity violation: " + ex.getMessage(), ex);

        // Try to extract constraint name for better message
        String message = "Data integrity constraint violated";
        if (ex.getMessage() != null && ex.getMessage().contains("unique")) {
            message = "Duplicate entry: resource already exists";
        } else if (ex.getMessage() != null && ex.getMessage().contains("foreign key")) {
            message = "Cannot complete operation: related resource does not exist";
        }

        return buildResponse(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException ex) {
        logException("error", "Database access error: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred");
    }

    @ExceptionHandler(TransactionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleTransaction(TransactionException ex) {
        logException("error", "Transaction error: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Transaction failed");
    }

    // ====== I/O AND SYSTEM EXCEPTIONS ======

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
        logException("error", "I/O error: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "System I/O error occurred");
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        logException("error", "Illegal state: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid application state");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        logException("warn", "Illegal argument: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request parameters");
    }

    // ====== CATCH-ALL ======

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logException("error", "Unexpected error: " + ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}