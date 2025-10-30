package com.litclub.Backend.security.roles.utility;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * A security class for handling club-level authentication
 *
 * <p>This class utilises Method Security to secure the methods
 * in the top level gatekeeper Services</p>
 */
@Component("clubSecurity")
public class ClubSecurityExpressionHandler {

    public boolean isOwner(Authentication auth, Long clubID) {
        return hasAuthority(auth, "CLUB_" + clubID + "_OWNER");
    }

    public boolean isModerator(Authentication auth, Long clubID) {
        return hasAuthority(auth, "CLUB_" + clubID + "_MODERATOR");
    }

    public boolean isMember(Authentication auth, Long clubID) {
        return hasAuthority(auth, "CLUB_" + clubID + "_MEMBER");
    }

    private boolean hasAuthority(Authentication auth, String role) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(role));
    }
}
