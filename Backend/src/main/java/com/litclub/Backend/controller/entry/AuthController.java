package com.litclub.Backend.controller.entry;

import com.litclub.Backend.construct.auth.AuthResponse;
import com.litclub.Backend.construct.user.UserLoginRecord;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.construct.user.UserRegistrationRecord;
import com.litclub.Backend.security.jwt.JwtService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller handling authentication endpoints for registration and login.
 *
 * <p>Provides endpoints for creating new users and authenticating existing ones.
 * On success, returns an {@link AuthResponse} containing a JWT token and user data.</p>
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li><b>POST</b> <code>/api/auth/register</code> — Register a new user</li>
 *   <li><b>POST</b> <code>/api/auth/login</code> — Log in an existing user</li>
 * </ul>
 *
 * @see UserService
 * @see JwtService
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user and issues a JWT token.
     *
     * @param userRegistrationRecord the registration payload containing username, password, and personal details
     * @return a response containing the generated JWT and the created user data
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody UserRegistrationRecord userRegistrationRecord) {
        UserRecord userRecord = userService.registerUser(userRegistrationRecord);
        String token = jwtService.generateToken(userRecord);
        return ResponseEntity.ok(new AuthResponse(token, userRecord));
    }

    /**
     * Authenticates an existing user using either username or email.
     *
     * @param userLoginRecord the login credentials (username/email and password)
     * @return a response containing the generated JWT and user data
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody UserLoginRecord userLoginRecord) {
        UserRecord userRecord = userService.login(userLoginRecord);
        String token = jwtService.generateToken(userRecord);
        return ResponseEntity.ok(new AuthResponse(token, userRecord));
    }
}
