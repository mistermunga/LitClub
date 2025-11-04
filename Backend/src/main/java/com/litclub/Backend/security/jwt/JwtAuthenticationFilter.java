package com.litclub.Backend.security.jwt;

import com.litclub.Backend.security.userdetails.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
/**
 * This filter intercepts incoming requests to validate JWT tokens.
 * It extracts the token from the Authorization header, validates it, and sets the authentication in the SecurityContext.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        /*
         * 1. Extract the Authorization header.
         * 2. If the header is missing or doesn't start with "Bearer ", continue the filter chain without authentication.
         * 3. Extract the JWT token from the header.
         * 4. Extract the username from the token.
         * 5. If a username is found and no authentication is currently set in the SecurityContext:
         *    a. Load user details using the customUserDetailsService.
         *    b. Validate the token.
         *    c. If valid, create an authentication token and set it in the SecurityContext.
         * 6. Continue the filter chain.
         */

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = jwtService.stripBearer(authorizationHeader);
        String username = jwtService.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = customUserDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
