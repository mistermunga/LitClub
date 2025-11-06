package com.litclub.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Singleton HTTP client for communicating with the LitClub backend.
 *
 * <p>Handles all network operations including:
 * <ul>
 *   <li>Request building and serialization</li>
 *   <li>JWT token storage and injection</li>
 *   <li>Response deserialization</li>
 *   <li>Error mapping and handling</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> This class is thread-safe and designed
 * as a singleton. All methods can be called from any thread.
 */
public class ApiClient {

    private static ApiClient instance;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    // Session state
    private String authToken;
    private Long currentUserId;

    // Private constructor for singleton
    private ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Gets the singleton instance of ApiClient.
     * Must call {@link #initialize(String)} before first use.
     */
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient not initialized. Call initialize() first.");
        }
        return instance;
    }

    /**
     * Initializes the singleton with the backend base URL.
     *
     * @param baseUrl the backend URL
     */
    public static synchronized void initialize(String baseUrl) {
        if (instance == null) {
            instance = new ApiClient(baseUrl);
        }
    }

    // ====== SESSION MANAGEMENT ======

    /**
     * Sets the authentication token for subsequent requests.
     *
     * @param token JWT token from login/register
     * @param userId the authenticated user's ID
     */
    public void setAuthToken(String token, Long userId) {
        this.authToken = token;
        this.currentUserId = userId;
    }

    /**
     * Clears the authentication token (logout).
     */
    public void clearAuthToken() {
        this.authToken = null;
        this.currentUserId = null;
    }

    /**
     * Checks if a user is currently authenticated.
     */
    public boolean isAuthenticated() {
        return authToken != null && !authToken.isEmpty();
    }

    /**
     * Gets the current user's ID.
     *
     * @return user ID or null if not authenticated
     */
    public Long getCurrentUserId() {
        return currentUserId;
    }

    // ====== HTTP METHODS ======

    /**
     * Performs a GET request.
     *
     * @param endpoint API endpoint path (e.g., "/api/books")
     * @param responseType class of the expected response
     * @return CompletableFuture with deserialized response
     */
    public <T> CompletableFuture<T> get(String endpoint, Class<T> responseType) {
        HttpRequest request = buildRequest(endpoint)
                .GET()
                .build();

        return sendRequest(request, responseType);
    }

    /**
     * Performs a POST request with a request body.
     *
     * @param endpoint API endpoint path
     * @param body request body object (will be serialized to JSON)
     * @param responseType class of the expected response
     * @return CompletableFuture with deserialized response
     */
    public <T> CompletableFuture<T> post(String endpoint, Object body, Class<T> responseType) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = buildRequest(endpoint)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendRequest(request, responseType);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(
                    new ApiException("Failed to serialize request body", e)
            );
        }
    }

    /**
     * Performs a POST request without a request body.
     *
     * @param endpoint API endpoint path
     * @param responseType class of the expected response
     * @return CompletableFuture with deserialized response
     */
    public <T> CompletableFuture<T> post(String endpoint, Class<T> responseType) {
        HttpRequest request = buildRequest(endpoint)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return sendRequest(request, responseType);
    }

    /**
     * Performs a PUT request with a request body.
     *
     * @param endpoint API endpoint path
     * @param body request body object (will be serialized to JSON)
     * @param responseType class of the expected response
     * @return CompletableFuture with deserialized response
     */
    public <T> CompletableFuture<T> put(String endpoint, Object body, Class<T> responseType) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = buildRequest(endpoint)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendRequest(request, responseType);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(
                    new ApiException("Failed to serialize request body", e)
            );
        }
    }

    /**
     * Performs a DELETE request.
     *
     * @param endpoint API endpoint path
     * @return CompletableFuture that completes when request finishes
     */
    public CompletableFuture<Void> delete(String endpoint) {
        HttpRequest request = buildRequest(endpoint)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        throw new ApiException(
                                "DELETE request failed with status " + response.statusCode(),
                                response.body()
                        );
                    }
                    return null;
                });
    }

    // ====== INTERNAL HELPERS ======

    /**
     * Builds a request with common headers and authentication.
     */
    private HttpRequest.Builder buildRequest(String endpoint) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30));

        // Add auth token if available
        if (authToken != null && !authToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        return builder;
    }

    /**
     * Sends the request and deserializes the response.
     */
    private <T> CompletableFuture<T> sendRequest(HttpRequest request, Class<T> responseType) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    // Handle HTTP errors
                    if (response.statusCode() >= 400) {
                        throw new ApiException(
                                "Request failed with status " + response.statusCode(),
                                response.body()
                        );
                    }

                    // Handle empty responses (204 No Content)
                    if (response.statusCode() == 204 || response.body().isEmpty()) {
                        return null;
                    }

                    // Deserialize response
                    try {
                        return objectMapper.readValue(response.body(), responseType);
                    } catch (Exception e) {
                        throw new ApiException(
                                "Failed to deserialize response",
                                e
                        );
                    }
                });
    }

    // ====== EXCEPTION CLASS ======

    /**
     * Custom exception for API-related errors.
     */
    public static class ApiException extends RuntimeException {
        private final String responseBody;

        public ApiException(String message, String responseBody) {
            super(message);
            this.responseBody = responseBody;
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
            this.responseBody = null;
        }

        public String getResponseBody() {
            return responseBody;
        }
    }
}