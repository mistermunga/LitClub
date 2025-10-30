package com.litclub.Backend.security.roles.utility;

import com.litclub.Backend.security.userdetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * A security class for handling Global instance security
 */
@Component("userSecurity")
public class UserSecurityExpressionHandler {

    public boolean isCurrentUser(Authentication auth, Long userId) {
        if (auth == null || auth.getPrincipal() == null) return false;
        if (!(auth.getPrincipal() instanceof CustomUserDetails cud)) return false;
        return cud.getUser().getUserID().equals(userId);
    }

    public boolean isAdmin(Authentication auth) {
        return hasRole(auth);
    }

    public boolean isCurrentUserOrAdmin(Authentication auth, Long userId) {
        return isCurrentUser(auth, userId) || hasRole(auth);
    }

    private boolean hasRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMINISTRATOR"));
    }
}

