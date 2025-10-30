package com.litclub.Backend.construct.auth;

import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.service.middle.UserService;

/**
 * Represents the response returned after a successful authentication event
 * (registration or login).
 *
 * <p>Encapsulates both the generated JWT token and the authenticated user's data.</p>
 *
 * @param token the JWT token used for authenticating future requests
 * @param userRecord the {@link UserRecord} of the authenticated user
 *
 * @see com.litclub.Backend.security.jwt.JwtService
 * @see UserService
 */
public record AuthResponse(String token, UserRecord userRecord) {}
