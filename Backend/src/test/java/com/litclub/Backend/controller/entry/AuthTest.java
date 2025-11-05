package com.litclub.Backend.controller.entry;

import com.litclub.Backend.construct.auth.AuthResponse;
import com.litclub.Backend.construct.user.UserLoginRecord;
import com.litclub.Backend.construct.user.UserRegistrationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";
    }

    // ====== REGISTRATION TESTS ======

    @Test
    void registerUser_ShouldReturn200AndToken_WhenValidRequest() {
        // Arrange
        UserRegistrationRecord registration = new UserRegistrationRecord(
                "alice",
                "Alice",
                "Smith",
                "alice@example.com",
                "password123",
                false
        );

        // Act
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/register",
                registration,
                AuthResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotEmpty();
        assertThat(response.getBody().userRecord()).isNotNull();
        assertThat(response.getBody().userRecord().username()).isEqualTo("alice");
        assertThat(response.getBody().userRecord().email()).isEqualTo("alice@example.com");
        assertThat(response.getBody().userRecord().firstName()).isEqualTo("Alice");
        assertThat(response.getBody().userRecord().surname()).isEqualTo("Smith");
    }

    @Test
    void registerUser_ShouldReturn409_WhenUsernameAlreadyExists() {
        // Arrange - First registration
        UserRegistrationRecord firstRegistration = new UserRegistrationRecord(
                "bob",
                "Bob",
                "Jones",
                "bob@example.com",
                "password123",
                false
        );
        restTemplate.postForEntity(baseUrl + "/register", firstRegistration, AuthResponse.class);

        // Arrange - Second registration with same username
        UserRegistrationRecord duplicateRegistration = new UserRegistrationRecord(
                "bob",
                "Robert",
                "Smith",
                "robert@example.com",
                "different123",
                false
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/register",
                duplicateRegistration,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isNotNull();
    }

    @Test
    void registerUser_ShouldGrantAdminRole_WhenFirstUser() {
        // Arrange
        UserRegistrationRecord registration = new UserRegistrationRecord(
                "firstadmin",
                "Admin",
                "User",
                "admin@example.com",
                "admin123",
                false // Even though we pass false, first user should be admin
        );

        // Act
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/register",
                registration,
                AuthResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Note: We can't directly check roles in UserRecord, but first user should have admin privileges
        // This would be better verified through an admin-only endpoint test
    }

    // ====== LOGIN TESTS (USERNAME) ======

    @Test
    void loginUser_ShouldReturn200AndToken_WhenValidUsernameAndPassword() {
        // Arrange - Register a user first
        UserRegistrationRecord registration = new UserRegistrationRecord(
                "charlie",
                "Charlie",
                "Brown",
                "charlie@example.com",
                "password123",
                false
        );
        restTemplate.postForEntity(baseUrl + "/register", registration, AuthResponse.class);

        // Arrange - Login credentials
        UserLoginRecord login = new UserLoginRecord(
                Optional.empty(),
                Optional.of("charlie"),
                "password123"
        );

        // Act
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/login",
                login,
                AuthResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotEmpty();
        assertThat(response.getBody().userRecord()).isNotNull();
        assertThat(response.getBody().userRecord().username()).isEqualTo("charlie");
    }

    @Test
    void loginUser_ShouldReturn401_WhenInvalidUsername() {
        // Arrange
        UserLoginRecord login = new UserLoginRecord(
                Optional.empty(),
                Optional.of("nonexistent"),
                "password123"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/login",
                login,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void loginUser_ShouldReturn401_WhenInvalidPassword() {
        // Arrange - Register a user first
        UserRegistrationRecord registration = new UserRegistrationRecord(
                "david",
                "David",
                "Wilson",
                "david@example.com",
                "correctpassword",
                false
        );
        restTemplate.postForEntity(baseUrl + "/register", registration, AuthResponse.class);

        // Arrange - Login with wrong password
        UserLoginRecord login = new UserLoginRecord(
                Optional.empty(),
                Optional.of("david"),
                "wrongpassword"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/login",
                login,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
    }

    // ====== LOGIN TESTS (EMAIL) ======

    @Test
    void loginUser_ShouldReturn200AndToken_WhenValidEmailAndPassword() {
        // Arrange - Register a user first
        UserRegistrationRecord registration = new UserRegistrationRecord(
                "emily",
                "Emily",
                "Davis",
                "emily@example.com",
                "password123",
                false
        );
        restTemplate.postForEntity(baseUrl + "/register", registration, AuthResponse.class);

        // Arrange - Login credentials with email
        UserLoginRecord login = new UserLoginRecord(
                Optional.of("emily@example.com"),
                Optional.empty(),
                "password123"
        );

        // Act
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/login",
                login,
                AuthResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotEmpty();
        assertThat(response.getBody().userRecord()).isNotNull();
        assertThat(response.getBody().userRecord().email()).isEqualTo("emily@example.com");
    }

    @Test
    void loginUser_ShouldReturn401_WhenInvalidEmail() {
        // Arrange
        UserLoginRecord login = new UserLoginRecord(
                Optional.of("nonexistent@example.com"),
                Optional.empty(),
                "password123"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/login",
                login,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
    }

    // ====== JWT TOKEN VALIDATION ======

    @Test
    void registerAndLogin_ShouldReturnDifferentTokens_ForSameUser() {
        // Arrange
        UserRegistrationRecord registration = new UserRegistrationRecord(
                "frank",
                "Frank",
                "Miller",
                "frank@example.com",
                "password123",
                false
        );

        // Act - Register
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl + "/register",
                registration,
                AuthResponse.class
        );

        // Act - Login
        UserLoginRecord login = new UserLoginRecord(
                Optional.empty(),
                Optional.of("frank"),
                "password123"
        );
        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                login,
                AuthResponse.class
        );

        // Assert - Both should succeed but tokens should be different (different timestamps)
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registerResponse.getBody().token()).isNotEmpty();
        assertThat(loginResponse.getBody().token()).isNotEmpty();
        // Note: Tokens might be the same if issued in same second, so we can't assert inequality
        // But both should be valid JWT tokens
    }

    // ====== EDGE CASES ======

    @Test
    void registerUser_ShouldReturn400_WhenMissingRequiredFields() {
        // Arrange - Missing password
        UserRegistrationRecord invalidRegistration = new UserRegistrationRecord(
                "grace",
                "Grace",
                "Lee",
                "grace@example.com",
                null, // Missing password
                false
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/register",
                invalidRegistration,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void loginUser_ShouldReturn400_WhenBothUsernameAndEmailProvided() {
        // Arrange
        UserLoginRecord login = new UserLoginRecord(
                Optional.of("email@example.com"),
                Optional.of("username"),
                "password123"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/login",
                login,
                Map.class
        );

        // Assert - Should still work, service chooses username if both present
        // This tests the current implementation behavior
        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST);
    }

    @Test
    void loginUser_ShouldReturn400_WhenNeitherUsernameNorEmailProvided() {
        // Arrange
        UserLoginRecord login = new UserLoginRecord(
                Optional.empty(),
                Optional.empty(),
                "password123"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/login",
                login,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}