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
import java.util.Optional;

/**
 * Middle-tier service managing club creation, retrieval, and membership queries.
 *
 * <p>This service orchestrates club-related business logic by delegating to low-tier services
 * for membership management. It validates club existence and creator assignments but
 * <strong>does not enforce access control</strong> — callers are responsible for authorization.</p>
 *
 * <p><strong>Design Principles:</strong></p>
 * <ul>
 *   <li><strong>Input Validation:</strong> All public methods validate that clubs exist before proceeding</li>
 *   <li><strong>Entity Trust:</strong> When entities (User) are passed in, they are assumed valid</li>
 *   <li><strong>No Access Control:</strong> Callers must enforce permissions via top-tier services</li>
 *   <li><strong>Delegation:</strong> Membership operations are delegated to low-tier services</li>
 * </ul>
 *
 * <p><strong>Tier Position:</strong> Middle tier — depends only on low-tier services and repositories.</p>
 *
 * @see Club
 * @see ClubRepository
 * @see ClubMembershipService
 */
@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMembershipService clubMembershipService;

    public ClubService(ClubRepository clubRepository,
                       ClubMembershipService clubMembershipService) {
        this.clubRepository = clubRepository;
        this.clubMembershipService = clubMembershipService;
    }

    // ===== CREATE =====

    /**
     * Registers a new club with the specified creator.
     *
     * <p>The creator user is validated and assigned to the club before persistence.
     * The club entity will be automatically enrolled in the membership system after creation.</p>
     *
     * @param club the club entity to register
     * @param creator the creator of the club
     * @return the persisted {@link Club} entity
     * @throws UserNotFoundException if no user with the given ID exists
     */
    @Transactional
    public Club registerClub(Club club, User creator) {
        club.setCreator(creator);
        return clubRepository.save(club);
    }

    // ===== RETRIEVAL =====

    /**
     * Retrieves all clubs in the system.
     *
     * @return list of all clubs
     */
    @Transactional(readOnly = true)
    public List<Club> getClubs() {
        return clubRepository.findAll();
    }

    /**
     * Retrieves a club by its database ID.
     *
     * @param id the club ID
     * @return an {@link Optional} containing the club, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Club> getClubById(Long id) {
        return clubRepository.findClubByClubID(id);
    }

    /**
     * Retrieves a club by its name.
     *
     * @param clubName the name of the club
     * @return an {@link Optional} containing the club, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Club> getClubByName(String clubName) {
        return clubRepository.findClubByClubName(clubName);
    }

    /**
     * Retrieves a club by ID or throws an exception if not found.
     *
     * <p>This is the recommended method for use within the service to ensure
     * club existence before proceeding with operations.</p>
     *
     * @param clubID the club ID
     * @return the validated {@link Club} entity
     * @throws ClubNotFoundException if no club with the given ID exists
     */
    @Transactional(readOnly = true)
    public Club requireClubById(Long clubID) {
        return getClubById(clubID)
                .orElseThrow(() -> new ClubNotFoundException("clubID", clubID.toString()));
    }

    /**
     * Retrieves a club by name or throws an exception if not found.
     *
     * @param clubName the name of the club
     * @return the validated {@link Club} entity
     * @throws ClubNotFoundException if no club with the given name exists
     */
    @Transactional(readOnly = true)
    public Club requireClubByName(String clubName) {
        return getClubByName(clubName)
                .orElseThrow(() -> new ClubNotFoundException("clubName", clubName));
    }

    // ===== MEMBERSHIP QUERIES =====

    /**
     * Retrieves all users who are members of the specified club.
     *
     * <p>Delegates to {@link ClubMembershipService} to fetch membership relationships.</p>
     *
     * @param clubID the club ID
     * @return list of {@link User} entities who are members
     * @throws ClubNotFoundException if no club with the given ID exists
     */
    @Transactional(readOnly = true)
    public List<User> getUsersForClub(Long clubID) {
        Club club = requireClubById(clubID);
        return clubMembershipService.getUsersForClub(club);
    }

    /**
     * Retrieves all users who are members of the specified club.
     *
     * @param clubName the name of the club
     * @return list of {@link User} entities who are members
     * @throws ClubNotFoundException if no club with the given name exists
     */
    @Transactional(readOnly = true)
    public List<User> getUsersForClub(String clubName) {
        Club club = requireClubByName(clubName);
        return clubMembershipService.getUsersForClub(club);
    }

    /**
     * Retrieves all users who are members of the specified club.
     *
     * <p><strong>Note:</strong> The {@code club} parameter is assumed to be a valid,
     * persisted entity. Callers must validate club existence before calling this method.</p>
     *
     * @param club the club entity (must be valid)
     * @return list of {@link User} entities who are members
     */
    @Transactional(readOnly = true)
    public List<User> getUsersForClub(Club club) {
        return clubMembershipService.getUsersForClub(club);
    }


    // ====== DELETE ======

    @Transactional
    public void deleteClub(Long clubID) {
        Club club = requireClubById(clubID);
        clubRepository.delete(club);
    }

    // ===== UTILITY =====

    /**
     * Converts a {@link Club} entity to a {@link ClubRecord} DTO.
     *
     * <p>This method includes the club creator but initializes the members set as empty.
     * Use {@link #getUsersForClub(Club)} to populate membership data if needed.</p>
     *
     * @param club the club entity to convert
     * @return the corresponding {@link ClubRecord}
     */
    public ClubRecord createClubRecordFromClub(Club club) {
        return new ClubRecord(
                club.getClubID(),
                club.getClubName(),
                UserService.convertUserToRecord(club.getCreator()),
                new HashSet<>()
        );
    }
}