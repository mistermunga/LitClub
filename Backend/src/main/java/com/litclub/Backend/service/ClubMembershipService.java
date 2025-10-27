package com.litclub.Backend.service;

import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.ClubMembership;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.entity.compositeKey.ClubMembershipID;
import com.litclub.Backend.exception.MembershipNotFoundException;
import com.litclub.Backend.repository.ClubMembershipRepository;
import com.litclub.Backend.security.roles.ClubRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service that manages the relationship between {@link User}s and {@link Club}s.
 *
 * <p>This service provides operations for enrolling users, managing roles,
 * retrieving memberships, and removing users from clubs.
 *
 * <p><strong>Responsibilities of the caller:</strong>
 * <ul>
 *   <li>Ensure all entities (e.g. {@code User}, {@code Club}) are verified before being passed in.</li>
 *   <li>Handle access control and authorization externally; this service does not enforce it.</li>
 * </ul>
 *
 * <p><strong>Thread safety:</strong> This service is stateless and therefore thread-safe.
 *
 * @see ClubMembership
 * @see ClubRole
 */

@Service
public class ClubMembershipService {

    private final ClubMembershipRepository clubMembershipRepository;

    public ClubMembershipService(ClubMembershipRepository clubMembershipRepository) {
        this.clubMembershipRepository = clubMembershipRepository;
    }

    // ====== CREATE ======
    @Transactional
    public ClubMembership enrollUserToClub(Club club, User user) {

        var oldMembership = clubMembershipRepository.findByClubAndUser(club, user);

        if (oldMembership.isPresent()) {
            return oldMembership.get();
        }

        Set<ClubRole> clubRoles = new HashSet<>();
        clubRoles.add(ClubRole.MEMBER);

        if (club.getCreator().equals(user)) {
            clubRoles.add(ClubRole.OWNER);
        }

        ClubMembershipID clubMembershipID = new ClubMembershipID(club.getClubID(), user.getUserID());
        ClubMembership membership = new ClubMembership();

        membership.setClubMembershipID(clubMembershipID);
        membership.setRoles(clubRoles);

        return clubMembershipRepository.save(membership);
    }

    // ====== READ ======
    @Transactional(readOnly = true)
    public ClubMembership getMembershipByClubAndUser(Club club, User user) {
        Optional<ClubMembership> membership = clubMembershipRepository.findByClubAndUser(club, user);
        return membership.orElseThrow(
                () -> new MembershipNotFoundException(user.getUsername(), club.getClubName())
        );
    }

    @Transactional(readOnly = true)
    public ClubMembership getMembershipByClubAndUser(ClubMembershipID clubMembershipID) {
        Optional<ClubMembership> membership = clubMembershipRepository.findClubMembershipByClubMembershipID(
                clubMembershipID
        );
        return membership.orElseThrow(
                () -> new MembershipNotFoundException(
                        clubMembershipID.getMemberID().toString(),
                        clubMembershipID.getClubID().toString()
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<ClubMembership> getAllClubMemberships() {
        return clubMembershipRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ClubMembership> getClubMembershipsByClub(Club club) {
        return clubMembershipRepository.findByClub(club);
    }

    @Transactional(readOnly = true)
    public List<ClubMembership> getClubMembershipsByUser(User user) {
        return clubMembershipRepository.findByMember(user);
    }

    @Transactional(readOnly = true)
    public List<Club> getClubsForUser(User user) {
        List<ClubMembership> clubMemberships = getClubMembershipsByUser(user);
        List<Club> clubs = new ArrayList<>();
        for (ClubMembership clubMembership : clubMemberships) {
            clubs.add(clubMembership.getClub());
        }
        return clubs;
    }

    @Transactional(readOnly = true)
    public List<User> getUsersForClub(Club club) {
        List<ClubMembership> clubMemberships = getClubMembershipsByClub(club);
        List<User> users = new ArrayList<>();
        for (ClubMembership clubMembership : clubMemberships) {
            users.add(clubMembership.getMember());
        }
        return users;
    }

    // ====== UPDATE ======
    @Transactional
    public ClubMembership modifyClubRole(Set<ClubRole> clubRoles, User user, Club club) {
        ClubMembership membership = getMembershipByClubAndUser(club, user);
        clubRoles.addAll(membership.getRoles());
        membership.setRoles(clubRoles);
        return clubMembershipRepository.save(membership);
    }

    // ====== DELETE ======
    @Transactional
    public void deRegisterUserFromClub(User user, Club club) {
        ClubMembership membership = getMembershipByClubAndUser(club, user);
        clubMembershipRepository.delete(membership);
    }
}
