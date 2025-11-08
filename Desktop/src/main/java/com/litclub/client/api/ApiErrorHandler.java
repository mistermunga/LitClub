package com.litclub.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Centralized error handler for API responses.
 *
 * <p>Translates API exceptions and HTTP status codes into user-friendly error messages
 * suitable for display in the UI. Handles backend error responses, network errors,
 * and provides fallback messages for unknown errors.</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * // In service classes:
 * .exceptionally(throwable -> {
 *     Platform.runLater(() -> {
 *         String errorMessage = ApiErrorHandler.parseError(throwable);
 *         onError.accept(errorMessage);
 *     });
 *     return null;
 * });
 * </pre>
 */
public class ApiErrorHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Prevent instantiation
    private ApiErrorHandler() {}

    /**
     * Parses any throwable from API operations into a user-friendly error message.
     *
     * @param throwable the exception thrown during API operation
     * @return user-friendly error message suitable for UI display
     */
    public static String parseError(Throwable throwable) {
        // Unwrap CompletionException if present
        Throwable actualError = unwrapException(throwable);

        // Check if it's an ApiException
        if (actualError instanceof ApiClient.ApiException apiException) {
            return parseApiException(apiException);
        }

        // Handle other exception types
        String message = actualError.getMessage();
        if (message != null) {
            return parseGenericError(message);
        }

        return "An unexpected error occurred. Please try again.";
    }

    /**
     * Unwraps CompletionException to get the actual cause.
     */
    private static Throwable unwrapException(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null &&
                current.getClass().getName().contains("CompletionException")) {
            current = current.getCause();
        }
        return current;
    }

    /**
     * Parses ApiException with HTTP status code and response body.
     */
    private static String parseApiException(ApiClient.ApiException apiException) {
        String message = apiException.getMessage();
        String responseBody = apiException.getResponseBody();

        // Try to extract status code from message
        Integer statusCode = extractStatusCode(message);

        // Try to parse backend error response if available
        if (responseBody != null && !responseBody.isEmpty()) {
            String backendMessage = parseBackendErrorResponse(responseBody);
            if (backendMessage != null) {
                return backendMessage;
            }
        }

        // Fall back to status code mapping
        if (statusCode != null) {
            return getMessageForStatusCode(statusCode);
        }

        // Ultimate fallback
        return "Request failed: " + (message != null ? message : "Unknown error");
    }

    /**
     * Attempts to extract HTTP status code from error message.
     * Looks for patterns like "status 404" or "with status 401".
     */
    private static Integer extractStatusCode(String message) {
        if (message == null) return null;

        // Try to find "status XXX" or "with status XXX"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("status\\s+(\\d{3})");
        java.util.regex.Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Parses JSON error response from backend (GlobalExceptionHandler format).
     * Expected format: {"timestamp": "...", "status": 400, "error": "...", "message": "..."}
     */
    private static String parseBackendErrorResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Check if response has the expected structure
            if (root.has("message")) {
                String backendMessage = root.get("message").asText();

                // Backend messages are already user-friendly, but we can enhance them
                if (root.has("status")) {
                    int status = root.get("status").asInt();
                    return enhanceBackendMessage(backendMessage, status);
                }

                return backendMessage;
            }
        } catch (Exception e) {
            // If JSON parsing fails, continue to fallback
            return null;
        }

        return null;
    }

    /**
     * Enhances backend error messages with additional context based on status code.
     */
    private static String enhanceBackendMessage(String backendMessage, int statusCode) {
        // For some status codes, the backend message is sufficient
        // For others, we might want to add context

        switch (statusCode) {
            case 401:
                // Backend might say "Invalid credentials"
                // We can leave it as is or enhance
                return backendMessage;

            case 403:
                // Backend might say "Access denied" or "Insufficient permissions"
                return backendMessage;

            case 404:
                // Backend messages like "User not found", "Book not found" are good
                return backendMessage;

            case 409:
                // Backend messages like "Username or email already in use" are good
                return backendMessage;

            case 500:
                // For server errors, we might want to soften the message
                return "Server error: " + backendMessage + ". Please try again later.";

            default:
                return backendMessage;
        }
    }

    /**
     * Maps HTTP status codes to user-friendly error messages.
     * Provides fallback messages when backend response parsing fails.
     */
    private static String getMessageForStatusCode(int statusCode) {
        return switch (statusCode) {
            // 1xx Informational (rarely used in error contexts)
            case 100 -> "Request is being processed. Please wait.";
            case 101 -> "Protocol switching. Please try again.";
            case 102 -> "Request is being processed. Please wait.";
            case 103 -> "Early hints received. Please wait.";

            // 2xx Success (shouldn't be errors, but just in case)
            case 200, 201, 202, 203, 204, 205, 206 ->
                    "Request completed successfully.";

            // 3xx Redirection
            case 300 -> "Multiple options available. Please refine your request.";
            case 301 -> "Resource has moved permanently. Please update your reference.";
            case 302 -> "Resource temporarily moved. Please try again.";
            case 303 -> "See other location for this resource.";
            case 304 -> "Content not modified.";
            case 307 -> "Resource temporarily redirected. Please try again.";
            case 308 -> "Resource permanently redirected. Please update your reference.";

            // 4xx Client Errors
            case 400 -> "Invalid request. Please check your input and try again.";
            case 401 -> "Authentication required. Please log in and try again.";
            case 402 -> "Payment required to access this resource.";
            case 403 -> "Access denied. You don't have permission to perform this action.";
            case 404 -> "Resource not found. Please check your request.";
            case 405 -> "Method not allowed for this resource.";
            case 406 -> "Request format not acceptable.";
            case 407 -> "Proxy authentication required.";
            case 408 -> "Request timeout. Please try again.";
            case 409 -> "Conflict detected. This resource may already exist.";
            case 410 -> "Resource no longer available.";
            case 411 -> "Content length required.";
            case 412 -> "Precondition failed. Please refresh and try again.";
            case 413 -> "Request too large. Please reduce the size and try again.";
            case 414 -> "Request URI too long.";
            case 415 -> "Unsupported media type.";
            case 416 -> "Range not satisfiable.";
            case 417 -> "Expectation failed.";
            case 418 -> "I'm a teapot. (This shouldn't happen!)";
            case 421 -> "Misdirected request.";
            case 422 -> "Request validation failed. Please check your input.";
            case 423 -> "Resource is locked.";
            case 424 -> "Failed dependency.";
            case 425 -> "Too early to process request.";
            case 426 -> "Upgrade required.";
            case 428 -> "Precondition required.";
            case 429 -> "Too many requests. Please slow down and try again later.";
            case 431 -> "Request header fields too large.";
            case 451 -> "Unavailable for legal reasons.";

            // 5xx Server Errors
            case 500 -> "Server error occurred. Please try again later.";
            case 501 -> "Feature not implemented.";
            case 502 -> "Bad gateway. Please try again later.";
            case 503 -> "Service temporarily unavailable. Please try again later.";
            case 504 -> "Gateway timeout. Please try again.";
            case 505 -> "HTTP version not supported.";
            case 506 -> "Variant also negotiates.";
            case 507 -> "Insufficient storage on server.";
            case 508 -> "Loop detected in request.";
            case 510 -> "Not extended.";
            case 511 -> "Network authentication required.";

            // Unknown status codes
            default -> {
                if (statusCode >= 400 && statusCode < 500) {
                    yield "Client error (" + statusCode + "). Please check your request.";
                } else if (statusCode >= 500 && statusCode < 600) {
                    yield "Server error (" + statusCode + "). Please try again later.";
                } else {
                    yield "Unexpected response (" + statusCode + ").";
                }
            }
        };
    }

    /**
     * Handles generic error messages (network errors, timeouts, etc.)
     * that are not ApiExceptions.
     */
    private static String parseGenericError(String message) {
        String lowerMessage = message.toLowerCase();

        // Network-related errors
        if (lowerMessage.contains("timeout") || lowerMessage.contains("timed out")) {
            return "Connection timeout. Please check your internet connection and try again.";
        }

        if (lowerMessage.contains("connection refused") ||
                lowerMessage.contains("unable to connect")) {
            return "Unable to connect to server. Please check your network connection.";
        }

        if (lowerMessage.contains("no route to host") ||
                lowerMessage.contains("host unreachable")) {
            return "Server unreachable. Please check your network connection.";
        }

        if (lowerMessage.contains("unknown host") ||
                lowerMessage.contains("nodename nor servname provided")) {
            return "Cannot find server. Please check your connection settings.";
        }

        // Serialization errors
        if (lowerMessage.contains("failed to serialize")) {
            return "Invalid request format. Please try again.";
        }

        if (lowerMessage.contains("failed to deserialize")) {
            return "Invalid response from server. Please contact support if this persists.";
        }

        // SSL/TLS errors
        if (lowerMessage.contains("ssl") ||
                lowerMessage.contains("certificate") ||
                lowerMessage.contains("handshake")) {
            return "Secure connection failed. Please check your network settings.";
        }

        // Generic fallback with sanitized message
        // Only include the message if it looks safe (no stack traces, etc.)
        if (message.length() < 100 && !message.contains("Exception") &&
                !message.contains("at ") && !message.contains("\n")) {
            return "Error: " + message;
        }

        return "An unexpected error occurred. Please try again.";
    }

    /**
     * Provides a user-friendly message for a specific HTTP status code.
     * Useful when you already have the status code extracted.
     *
     * @param statusCode HTTP status code
     * @return user-friendly error message
     */
    public static String getMessageForStatus(int statusCode) {
        return getMessageForStatusCode(statusCode);
    }

    /**
     * Checks if an error is authentication-related (401, 403).
     * Useful for triggering automatic logout or re-authentication flows.
     *
     * @param throwable the exception to check
     * @return true if the error is authentication-related
     */
    public static boolean isAuthenticationError(Throwable throwable) {
        Throwable actualError = unwrapException(throwable);

        if (actualError instanceof ApiClient.ApiException apiException) {
            String message = apiException.getMessage();
            Integer statusCode = extractStatusCode(message);
            return statusCode != null && (statusCode == 401 || statusCode == 403);
        }

        return false;
    }

    /**
     * Checks if an error is a network connectivity issue.
     * Useful for displaying different UI (e.g., "Check your connection" banner).
     *
     * @param throwable the exception to check
     * @return true if the error is network-related
     */
    public static boolean isNetworkError(Throwable throwable) {
        Throwable actualError = unwrapException(throwable);
        String message = actualError.getMessage();

        if (message == null) return false;

        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("timeout") ||
                lowerMessage.contains("connection refused") ||
                lowerMessage.contains("unable to connect") ||
                lowerMessage.contains("no route to host") ||
                lowerMessage.contains("unknown host");
    }

    /**
     * Checks if an error is a server error (5xx).
     * Useful for triggering retry logic or displaying maintenance messages.
     *
     * @param throwable the exception to check
     * @return true if the error is a server error
     */
    public static boolean isServerError(Throwable throwable) {
        Throwable actualError = unwrapException(throwable);

        if (actualError instanceof ApiClient.ApiException apiException) {
            String message = apiException.getMessage();
            Integer statusCode = extractStatusCode(message);
            return statusCode != null && statusCode >= 500 && statusCode < 600;
        }

        return false;
    }
}