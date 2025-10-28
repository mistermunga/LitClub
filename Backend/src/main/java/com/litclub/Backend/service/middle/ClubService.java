package com.litclub.Backend.service.middle;

import com.litclub.Backend.construct.club.ClubRecord;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.exception.ClubNotFoundException;
import com.litclub.Backend.exception.UserNotFoundException;
import com.litclub.Backend.repository.ClubRepository;
import com.litclub.Backend.service.low.ClubMembershipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

/**
 * Service that manages club creation, retrieval, and membership queries.
 *
 * <p>This service acts as the core entry point for all club-related business logic,
 * including club registration, validation, and membership retrieval. This is a
 * middle tier Service meaning the caller <strong>must enforce access control;
 * this Service does not enforce it</strong></p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Register new clubs with creator assignment</li>
 *   <li>Retrieve clubs by ID or name</li>
 *   <li>Query club memberships</li>
 *   <li>Convert clubs to data transfer records</li>
 * </ul>
 *
 * @see Club
 * @see ClubRepository
 * @see ClubMembershipService
 */
@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final UserService userService;
    private final ClubMembershipService clubMembershipService;

    public ClubService(ClubRepository clubRepository,
                       UserService userService,
                       ClubMembershipService clubMembershipService) {
        this.clubRepository = clubRepository;
        this.userService = userService;
        this.clubMembershipService = clubMembershipService;
    }

    // ====== CREATE ======

    /**
     * Registers a new club and assigns a creator.
     *
     * @param club the club entity to register
     * @param userID the ID of the user creating the club
     * @return the persisted {@link Club} entity
     * @throws UserNotFoundException if no user with the given ID exists
     */
    @Transactional
    public Club registerClub(Club club, Long userID) {
        var userOpt = userService.getUserById(userID);

        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("userID", userID.toString());
        }

        club.setCreator(userOpt.get());
        return clubRepository.save(club);
    }

    // ====== READ ======

    /** Retrieves all clubs in the system. */
    @Transactional(readOnly = true)
    public List<Club> getClubs() {
        return clubRepository.findAll();
    }

    /**
     * Retrieves a club by its database ID.
     *
     * @param id the club ID
     * @return the {@link Club} entity
     * @throws ClubNotFoundException if no club with the given ID exists
     */
    @Transactional(readOnly = true)
    public Club getClubById(Long id) {
        var clubOpt = clubRepository.findClubByClubID(id);

        if (clubOpt.isEmpty()) {
            throw new ClubNotFoundException("clubID", id.toString());
        }

        return clubOpt.get();
    }

    /**
     * Retrieves a club by its name.
     *
     * @param clubName the name of the club
     * @return the {@link Club} entity
     * @throws ClubNotFoundException if no club with the given name exists
     */
    @Transactional(readOnly = true)
    public Club getClubByName(String clubName) {
        var clubOpt = clubRepository.findClubByClubName(clubName);

        if (clubOpt.isEmpty()) {
            throw new ClubNotFoundException("clubName", clubName);
        }

        return clubOpt.get();
    }

    /**
     * Retrieves all users who are members of a club by club ID.
     *
     * @param clubID the club ID
     * @return list of {@link User} entities
     * @throws ClubNotFoundException if no club with the given ID exists
     */
    @Transactional(readOnly = true)
    public List<User> getUsersForClub(Long clubID) {
        Club club = getClubById(clubID);
        return clubMembershipService.getUsersForClub(club);
    }

    /**
     * Retrieves all users who are members of a club by club name.
     *
     * @param clubName the name of the club
     * @return list of {@link User} entities
     * @throws ClubNotFoundException if no club with the given name exists
     */
    @Transactional(readOnly = true)
    public List<User> getUsersForClub(String clubName) {
        Club club = getClubByName(clubName);
        return clubMembershipService.getUsersForClub(club);
    }

    // ====== UTILITY ======

    /**
     * Converts a {@link Club} entity to a {@link ClubRecord}.
     *
     * @param club the club entity to convert
     * @return the corresponding {@link ClubRecord}
     */
    public ClubRecord createClubRecordFromClub(Club club) {
        return new ClubRecord(
                club.getClubID(),
                club.getClubName(),
                userService.convertUserToRecord(club.getCreator()),
                new HashSet<>()
        );
    }
}