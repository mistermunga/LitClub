package com.litclub.Backend.security.roles.utility;

import com.litclub.Backend.entity.ClubMembership;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.security.roles.ClubRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class AuthorityMapper {

    private AuthorityMapper() {}

    public static Set<GrantedAuthority> mapToAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (user.getGlobalRoles() != null) {
            authorities.addAll(user.getGlobalRoles()
                    .stream()
                    .map(r -> new SimpleGrantedAuthority(r.getAuthority()))
                    .collect(Collectors.toSet()));
        }

        if (user.getMemberships() != null) {
            for (ClubMembership cm : user.getMemberships()) {
                Long clubId = cm.getClub().getClubID();
                Set<ClubRole> declared = cm.getRoles() == null ? Set.of() : cm.getRoles();
                Set<ClubRole> effective = expandWithImpliedRoles(declared);
                for (ClubRole cr : effective) {
                    String auth = String.format("CLUB_%d_%s", clubId, cr.name());
                    authorities.add(new SimpleGrantedAuthority(auth));
                }
            }
        }

        return authorities;
    }

    private static Set<ClubRole> expandWithImpliedRoles(Set<ClubRole> declared) {
        Set<ClubRole> s = new HashSet<>(declared);
        if (declared.contains(ClubRole.OWNER)) {
            // Owner implies Moderator and Member
            s.add(ClubRole.MODERATOR);
            s.add(ClubRole.MEMBER);
        }
        if (declared.contains(ClubRole.MODERATOR)) {
            // Moderator implies Member
            s.add(ClubRole.MEMBER);
        }
        return s;
    }
}
