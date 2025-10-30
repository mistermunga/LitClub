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
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUser().getUserID().equals(userId);
        }
        return false;
    }


    public boolean isCurrentUserOrAdmin(Authentication auth, Long userId) {
        return isCurrentUser(auth, userId) || hasRole(auth);
    }

    private boolean hasRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .allMatch(a -> a.equals("ROLE_ADMINISTRATOR"));
    }
}
