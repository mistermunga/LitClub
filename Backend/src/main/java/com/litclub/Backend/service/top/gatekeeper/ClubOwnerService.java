package com.litclub.Backend.service.top.gatekeeper;

import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.ClubMembership;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.exception.MembershipNotFoundException;
import com.litclub.Backend.security.roles.ClubRole;
import com.litclub.Backend.security.userdetails.CustomUserDetails;
import com.litclub.Backend.service.low.ClubMembershipService;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ClubOwnerService {

    private final ClubService clubService;
    private final UserService userService;
    private final ClubMembershipService membershipService;

    public ClubOwnerService(ClubService clubService,
                            ClubMembershipService membershipService,
                            UserService userService) {
        this.clubService = clubService;
        this.membershipService = membershipService;
        this.userService = userService;
    }

    // ====== DESTRUCTIVE CLUB ACTIONS ======
    @Transactional
    @PreAuthorize("@clubSecurity.isOwner(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public void deleteClub(Long clubID) {
        clubService.deleteClub(clubID);
    }

    // ====== OWNERSHIP MANAGEMENT ======
    @Transactional
    @PreAuthorize("@clubSecurity.isOwner(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public void transferOwnership(
            Long clubID,
            Long userID,
            boolean completeTransfer,
            boolean unmakeModerator,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Club club = clubService.requireClubById(clubID);
        User oldOwner = resolveOldOwner(club, customUserDetails);
        User newOwner = userService.requireUserById(userID);

        if (oldOwner.equals(newOwner)) {
            throw new IllegalArgumentException("Cannot transfer ownership to the same user.");
        }

        promoteNewOwner(club, newOwner);

        if (completeTransfer) {
            demoteOldOwner(club, oldOwner, unmakeModerator);
        }
    }

    private User resolveOldOwner(Club club, CustomUserDetails currentUser) {
        try {
            User user = currentUser.getUser();
            membershipService.getMembershipByClubAndUser(club, user);
            return user;
        } catch (MembershipNotFoundException e) {
            // Admin-triggered path
            List<ClubMembership> owners = membershipService.getMembershipsByClubAndRole(club, ClubRole.OWNER);
            if (owners.size() != 1) {
                throw new IllegalStateException(
                        "Expected exactly one owner for club '%s'; found %d."
                                .formatted(club.getClubName(), owners.size())
                );
            }
            return owners.getFirst().getMember();
        }
    }

    private void promoteNewOwner(Club club, User newOwner) {
        membershipService.modifyClubRole(Set.of(ClubRole.OWNER), newOwner, club);
    }

    private void demoteOldOwner(Club club, User oldOwner, boolean unmakeModerator) {
        membershipService.removeClubRole(Set.of(ClubRole.OWNER), oldOwner, club);

        if (unmakeModerator) {
            membershipService.removeClubRole(Set.of(ClubRole.MODERATOR), oldOwner, club);
        }
    }

    // ====== MODERATOR MANAGEMENT ======
    @Transactional
    @PreAuthorize("@clubSecurity.isOwner(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public void promoteToModerator(Long clubID, Long userID) {
        Club club = clubService.requireClubById(clubID);
        User user = userService.requireUserById(userID);
        membershipService.modifyClubRole(Set.of(ClubRole.MODERATOR), user, club);
    }

    @Transactional
    @PreAuthorize("@clubSecurity.isOwner(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public void demoteModerator(Long clubID, Long userID) {
        Club club = clubService.requireClubById(clubID);
        User user = userService.requireUserById(userID);
        membershipService.removeClubRole(Set.of(ClubRole.MODERATOR), user, club);
    }

}
