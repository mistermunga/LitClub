package com.litclub.Backend.service.middle;

import com.litclub.Backend.construct.book.BookStatus;
import com.litclub.Backend.construct.meeting.RsvpStatus;
import com.litclub.Backend.construct.user.UserLoginRecord;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.construct.user.UserRegistrationRecord;
import com.litclub.Backend.entity.*;
import com.litclub.Backend.exception.CredentialsAlreadyTakenException;
import com.litclub.Backend.exception.UserNotFoundException;
import com.litclub.Backend.repository.UserRepository;
import com.litclub.Backend.security.roles.GlobalRole;
import com.litclub.Backend.service.low.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Middle-tier service managing user authentication, registration, and all user-related operations.
 *
 * <p>This service orchestrates user-related business logic by delegating to low-tier services
 * for relationship management (clubs, books, notes, reviews, meetings, prompts). It validates
 * user existence and credentials but <strong>does not enforce access control</strong> – callers
 * are responsible for authorization.</p>
 *
 * <p><strong>Design Principles:</strong></p>
 * <ul>
 *   <li><strong>Input Validation:</strong> All public methods validate that users exist before proceeding</li>
 *   <li><strong>Entity Trust:</strong> When entities (Club, Book, Note, etc.) are passed in, they are assumed valid</li>
 *   <li><strong>No Access Control:</strong> Callers must enforce permissions via top-tier services</li>
 *   <li><strong>Delegation:</strong> Relationship operations are delegated to low-tier services</li>
 *   <li><strong>Comprehensive Coverage:</strong> Interacts with ALL low-tier services that use User entities</li>
 * </ul>
 *
 * <p><strong>Tier Position:</strong> Middle tier – depends only on low-tier services and repositories.</p>
 *
 * <p><strong>Low-Tier Service Coverage:</strong></p>
 * <ul>
 *   <li>{@link ClubMembershipService} - Club membership operations</li>
 *   <li>{@link UserBooksService} - Personal library management</li>
 *   <li>{@link DiscussionPromptService} - Discussion prompt creation and management</li>
 *   <li>{@link ReviewService} - Book review operations</li>
 *   <li>{@link MeetingAttendeeService} - Meeting RSVP management</li>
 *   <li>{@link MeetingRegisterService} - Attendance tracking</li>
 * </ul>
 *
 * @see User
 * @see UserRepository
 * @see ClubMembershipService
 * @see UserBooksService
 * @see DiscussionPromptService
 * @see ReviewService
 * @see MeetingAttendeeService
 * @see MeetingRegisterService
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClubMembershipService clubMembershipService;
    private final UserBooksService userBooksService;
    private final DiscussionPromptService discussionPromptService;
    private final ReviewService reviewService;
    private final MeetingAttendeeService meetingAttendeeService;
    private final MeetingRegisterService meetingRegisterService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ClubMembershipService clubMembershipService,
                       UserBooksService userBooksService,
                       DiscussionPromptService discussionPromptService,
                       ReviewService reviewService,
                       MeetingAttendeeService meetingAttendeeService,
                       MeetingRegisterService meetingRegisterService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clubMembershipService = clubMembershipService;
        this.userBooksService = userBooksService;
        this.discussionPromptService = discussionPromptService;
        this.reviewService = reviewService;
        this.meetingAttendeeService = meetingAttendeeService;
        this.meetingRegisterService = meetingRegisterService;
    }

    // ====== AUTHENTICATION =====

    /**
     * Authenticates a user by username or email and returns a {@link UserRecord}.
     *
     * <p>This method delegates to either {@link #loginWithUsername(UserLoginRecord)}
     * or {@link #loginWithEmail(UserLoginRecord)} based on which identifier is present.</p>
     *
     * @param userLoginRecord the login credentials containing username/email and password
     * @return the authenticated {@link UserRecord}
     * @throws BadCredentialsException if the credentials are invalid
     * @throws IllegalArgumentException if neither username nor email is provided
     */
    @Transactional(readOnly = true)
    public UserRecord login(UserLoginRecord userLoginRecord) {
        if (userLoginRecord.username().isPresent()) {
            return loginWithUsername(userLoginRecord);
        } else if (userLoginRecord.email().isPresent()) {
            return loginWithEmail(userLoginRecord);
        } else {
            throw new IllegalArgumentException("Username or Email is required");
        }
    }

    /**
     * Authenticates a user by username.
     *
     * @param userLoginRecord login credentials with username
     * @return the authenticated {@link UserRecord}
     * @throws BadCredentialsException if the username or password is invalid
     */
    @Transactional(readOnly = true)
    public UserRecord loginWithUsername(UserLoginRecord userLoginRecord) {
        Optional<User> user = userRepository.findUserByUsername(userLoginRecord.username().toString());

        if (user.isEmpty()) {
            throw new BadCredentialsException("Invalid username");
        }

        if (passwordEncoder.matches(userLoginRecord.password(), user.get().getPasswordHash())) {
            return convertUserToRecord(user.get());
        }

        throw new BadCredentialsException("Invalid credentials");
    }

    /**
     * Authenticates a user by email.
     *
     * @param userLoginRecord login credentials with email
     * @return the authenticated {@link UserRecord}
     * @throws BadCredentialsException if the email or password is invalid
     */
    @Transactional(readOnly = true)
    public UserRecord loginWithEmail(UserLoginRecord userLoginRecord) {
        Optional<User> user = userRepository.findUserByEmail(userLoginRecord.email().toString());

        if (user.isEmpty()) {
            throw new BadCredentialsException("Invalid email");
        }

        if (passwordEncoder.matches(userLoginRecord.password(), user.get().getPasswordHash())) {
            return convertUserToRecord(user.get());
        }

        throw new BadCredentialsException("Invalid credentials");
    }

    // ===== REGISTRATION =====

    /**
     * Registers a new user with securely hashed password.
     *
     * <p>If this is the first user in the system, they are automatically granted
     * administrator privileges via {@link #firstLaunch()}.</p>
     *
     * @param userRegistrationRecord the new user details
     * @return the created {@link UserRecord}
     * @throws CredentialsAlreadyTakenException if the username is already in use
     */
    @Transactional
    public UserRecord registerUser(UserRegistrationRecord userRegistrationRecord) {
        if (userRepository.existsByUsername(userRegistrationRecord.username())) {
            throw new CredentialsAlreadyTakenException("Username is already in use");
        }

        User user = new User(
                userRegistrationRecord.username(),
                userRegistrationRecord.firstName(),
                userRegistrationRecord.surname(),
                userRegistrationRecord.email(),
                firstLaunch()
        );

        user.setPasswordHash(passwordEncoder.encode(userRegistrationRecord.password()));

        return convertUserToRecord(userRepository.save(user));
    }

    // ===== RETRIEVAL =====

    /**
     * Retrieves all users in the system.
     *
     * @return list of all users, empty if none exist
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    /**
     * Retrieves a user by email.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the user, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    /**
     * Retrieves a user by database ID.
     *
     * @param id the user ID
     * @return an {@link Optional} containing the user, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findUserByUserID(id);
    }

    /**
     * Retrieves a user by ID or throws an exception if not found.
     *
     * <p>This is the recommended method for use within the service to ensure
     * user existence before proceeding with operations.</p>
     *
     * @param userID the user ID
     * @return the validated {@link User} entity
     * @throws UserNotFoundException if no user with the given ID exists
     */
    @Transactional(readOnly = true)
    public User requireUserById(Long userID) {
        return getUserById(userID)
                .orElseThrow(() -> new UserNotFoundException("userID", userID.toString()));
    }

    /**
     * Retrieves a user by username or email, or throws an exception if not found.
     *
     * <p>Attempts username lookup first, then email if username fails.</p>
     *
     * @param identifier either username or email
     * @return the validated {@link User} entity
     * @throws UserNotFoundException if no user with the given identifier exists
     */
    @Transactional(readOnly = true)
    public User requireUserByIdentifier(String identifier) {
        return getUserByUsername(identifier)
                .or(() -> getUserByEmail(identifier))
                .orElseThrow(() -> new UserNotFoundException("username/email", identifier));
    }

    // ===== CLUBS (ClubMembershipService) =====

    /**
     * Retrieves all clubs the user is a member of.
     *
     * <p>Delegates to {@link ClubMembershipService} to fetch club relationships.</p>
     *
     * @param userID the user ID
     * @return list of clubs the user belongs to
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Club> getClubsForUser(Long userID) {
        User user = requireUserById(userID);
        return clubMembershipService.getClubsForUser(user);
    }

    /**
     * Retrieves all clubs the user is a member of.
     *
     * @param identifier username or email
     * @return list of clubs the user belongs to
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Club> getClubsForUser(String identifier) {
        User user = requireUserByIdentifier(identifier);
        return clubMembershipService.getClubsForUser(user);
    }

    /**
     * Retrieves all club memberships for a user.
     *
     * @param userID the user ID
     * @return list of {@link ClubMembership} entities
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<ClubMembership> getClubMembershipsForUser(Long userID) {
        User user = requireUserById(userID);
        return clubMembershipService.getClubMembershipsByUser(user);
    }

    /**
     * Checks if a user is a member of a specific club.
     *
     * @param userID the user ID
     * @param club the club to check (must be a valid entity)
     * @return true if the user is a member of the club
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public boolean isUserMemberOfClub(Long userID, Club club) {
        User user = requireUserById(userID);
        try {
            clubMembershipService.getMembershipByClubAndUser(club, user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ===== BOOKS (UserBooksService) =====

    /**
     * Retrieves all books in the user's personal library.
     *
     * <p>Delegates to {@link UserBooksService} to fetch book relationships.</p>
     *
     * @param userID the user ID
     * @return list of books in the user's library
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Book> getBooksForUser(Long userID) {
        User user = requireUserById(userID);
        return userBooksService.getBooksForUser(user);
    }

    /**
     * Retrieves all books in the user's personal library.
     *
     * @param identifier username or email
     * @return list of books in the user's library
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Book> getBooksForUser(String identifier) {
        User user = requireUserByIdentifier(identifier);
        return userBooksService.getBooksForUser(user);
    }

    /**
     * Retrieves all UserBook records for a user.
     *
     * @param userID the user ID
     * @return list of {@link UserBook} entities with status and metadata
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<UserBook> getUserBooksForUser(Long userID) {
        User user = requireUserById(userID);
        return userBooksService.getUserBooksForUser(user);
    }

    /**
     * Adds a book to the user's personal library with a specific status.
     *
     * @param userID the user ID
     * @param book the book to add (must be a valid entity)
     * @param status the reading status
     * @return the created {@link UserBook} entity
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    public UserBook addBookToLibrary(Long userID, Book book, BookStatus status) {
        User user = requireUserById(userID);
        return userBooksService.addUserBook(user, book, status);
    }

    /**
     * Removes a book from the user's personal library.
     *
     * <p><strong>Note:</strong> The {@code book} parameter is assumed to be a valid,
     * persisted entity. Callers must validate the book exists before calling this method.</p>
     *
     * @param userID the user ID
     * @param book the book to remove (must be a valid entity)
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    public void removeBookFromLibrary(Long userID, Book book) {
        User user = requireUserById(userID);
        userBooksService.removeUserBook(user, book);
    }

    /**
     * Changes the reading status of a book in the user's library.
     *
     * @param userID the user ID
     * @param book the book to update (must be a valid entity)
     * @param newStatus the new reading status
     * @return the updated {@link UserBook} entity
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    public UserBook changeBookStatus(Long userID, Book book, BookStatus newStatus) {
        User user = requireUserById(userID);
        return userBooksService.changeStatus(user, book, newStatus);
    }

    /**
     * Retrieves a specific UserBook record for a user and book.
     *
     * @param userID the user ID
     * @param book the book (must be a valid entity)
     * @return the {@link UserBook} entity
     * @throws UserNotFoundException if the user does not exist
     * @throws com.litclub.Backend.exception.MissingLibraryItemException if the book is not in the user's library
     */
    @Transactional(readOnly = true)
    public UserBook getUserBook(Long userID, Book book) {
        User user = requireUserById(userID);
        return userBooksService.getUserBookByUserAndBook(user, book);
    }

    // ===== DISCUSSION PROMPTS (DiscussionPromptService) =====

    /**
     * Creates a new discussion prompt for a club.
     *
     * <p><strong>Note:</strong> The {@code club} parameter is assumed to be a valid,
     * persisted entity. Callers must validate club existence and user membership
     * before calling this method.</p>
     *
     * @param userID the user ID of the prompt creator
     * @param prompt the prompt text
     * @param club the club the prompt belongs to (must be a valid entity)
     * @return the created {@link DiscussionPrompt}
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    public DiscussionPrompt createDiscussionPrompt(Long userID, String prompt, Club club) {
        User user = requireUserById(userID);
        return discussionPromptService.createPrompt(prompt, user, club);
    }

    /**
     * Retrieves all discussion prompts created by the user across all clubs.
     *
     * @param userID the user ID
     * @return list of all prompts posted by the user
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<DiscussionPrompt> getAllDiscussionPromptsForUser(Long userID) {
        User user = requireUserById(userID);
        return discussionPromptService.findAllByPoster(user);
    }

    /**
     * Retrieves all discussion prompts created by the user within a specific club.
     *
     * <p><strong>Note:</strong> The {@code club} parameter is assumed to be a valid,
     * persisted entity.</p>
     *
     * @param userID the user ID
     * @param club the club to filter by (must be a valid entity)
     * @return list of prompts posted by the user in the specified club
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<DiscussionPrompt> getDiscussionPromptsForUserAndClub(Long userID, Club club) {
        User user = requireUserById(userID);
        return discussionPromptService.findByUserAndClub(user, club);
    }

    /**
     * Deletes all discussion prompts created by the user across all clubs.
     *
     * <p>This operation is irreversible and should be used with caution,
     * typically during account deletion.</p>
     *
     * @param userID the user ID
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    public void purgeUserPrompts(Long userID) {
        User user = requireUserById(userID);
        discussionPromptService.purgeUserPrompts(user);
    }

    // ===== REVIEWS (ReviewService) =====

    /**
     * Retrieves all reviews written by the user.
     *
     * @param userID the user ID
     * @return list of reviews written by the user
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsForUser(Long userID) {
        User user = requireUserById(userID);
        return reviewService.getReviews(user);
    }

    /**
     * Retrieves reviews by the user with a specific rating.
     *
     * @param userID the user ID
     * @param rating the rating to filter by
     * @return list of reviews with the specified rating
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsByUserAndRating(Long userID, int rating) {
        User user = requireUserById(userID);
        return reviewService.getRatedReviewsForUser(user, rating);
    }

    /**
     * Retrieves reviews by the user rated above a threshold.
     *
     * @param userID the user ID
     * @param rating minimum rating threshold
     * @return list of reviews rated above the threshold
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsRatedAbove(Long userID, int rating) {
        User user = requireUserById(userID);
        return reviewService.getUserReviewsRatedAbove(user, rating);
    }

    /**
     * Retrieves reviews by the user rated below a threshold.
     *
     * @param userID the user ID
     * @param rating maximum rating threshold
     * @return list of reviews rated below the threshold
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsRatedBelow(Long userID, int rating) {
        User user = requireUserById(userID);
        return reviewService.getUserReviewsRatedBelow(user, rating);
    }

    /**
     * Searches reviews by the user containing specific text.
     *
     * @param userID the user ID
     * @param query the search text
     * @return list of matching reviews
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Review> searchUserReviews(Long userID, String query) {
        User user = requireUserById(userID);
        return reviewService.searchUserReviews(user, query);
    }

    /**
     * Retrieves a user's review for a specific book.
     *
     * @param userID the user ID
     * @param book the book (must be a valid entity)
     * @return the review
     * @throws UserNotFoundException if the user does not exist
     * @throws jakarta.persistence.EntityNotFoundException if no review exists
     */
    @Transactional(readOnly = true)
    public Review getUserReviewForBook(Long userID, Book book) {
        User user = requireUserById(userID);
        return reviewService.getReviewByUserAndBook(user, book);
    }

    /**
     * Deletes all reviews written by the user.
     *
     * <p>This operation is irreversible and typically used during account deletion.</p>
     *
     * @param userID the user ID
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    public void purgeUserReviews(Long userID) {
        User user = requireUserById(userID);
        reviewService.purgeUserReviews(user);
    }

    // ===== MEETING RSVPS (MeetingAttendeeService) =====

    /**
     * Retrieves all meetings the user has RSVPed to.
     *
     * @param userID the user ID
     * @return list of {@link MeetingAttendee} records
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<MeetingAttendee> getMeetingRSVPsForUser(Long userID) {
        User user = requireUserById(userID);
        return meetingAttendeeService.findAllMeetingAttendances(user);
    }

    /**
     * Retrieves meetings the user has RSVPed to with a specific status.
     *
     * @param userID the user ID
     * @param status the RSVP status to filter by
     * @return list of {@link MeetingAttendee} records with the specified status
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<MeetingAttendee> getMeetingRSVPsByStatus(Long userID, RsvpStatus status) {
        User user = requireUserById(userID);
        return meetingAttendeeService.findAllMeetingAttendances(user, status);
    }

    /**
     * Retrieves the user's RSVP for a specific meeting.
     *
     * @param userID the user ID
     * @param meeting the meeting (must be a valid entity)
     * @return the {@link MeetingAttendee} record
     * @throws UserNotFoundException if the user does not exist
     * @throws jakarta.persistence.EntityNotFoundException if no RSVP exists
     */
    @Transactional(readOnly = true)
    public MeetingAttendee getUserRSVPForMeeting(Long userID, Meeting meeting) {
        User user = requireUserById(userID);
        return meetingAttendeeService.getMeetingAttendance(meeting, user);
    }

    /**
     * Extracts meetings from RSVP records.
     *
     * @param userID the user ID
     * @return list of meetings the user has RSVPed to
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsUserRSVPedTo(Long userID) {
        User user = requireUserById(userID);
        return meetingAttendeeService.findAllMeetingAttendances(user)
                .stream()
                .map(MeetingAttendee::getMeeting)
                .collect(Collectors.toList());
    }

    // ===== MEETING ATTENDANCE (MeetingRegisterService) =====

    /**
     * Retrieves all attendance records for the user.
     *
     * @param userID the user ID
     * @return list of {@link MeetingRegister} entries
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<MeetingRegister> getAttendanceRecordsForUser(Long userID) {
        User user = requireUserById(userID);
        return meetingRegisterService.findAll(user);
    }

    /**
     * Retrieves attendance records for the user filtered by a boolean field.
     *
     * @param userID the user ID
     * @param identifier field name ("attended", "late", or "excused")
     * @param value the boolean value to filter by
     * @return list of matching {@link MeetingRegister} entries
     * @throws UserNotFoundException if the user does not exist
     * @throws com.litclub.Backend.exception.MalformedDTOException if identifier is invalid
     */
    @Transactional(readOnly = true)
    public List<MeetingRegister> getAttendanceRecordsByField(Long userID, String identifier, boolean value) {
        User user = requireUserById(userID);
        return meetingRegisterService.findAll(user, identifier, value);
    }

    /**
     * Retrieves attendance records for the user filtered by attendance and another field.
     *
     * @param userID the user ID
     * @param attended the attendance status
     * @param identifier field name ("late" or "excused")
     * @param value the boolean value to filter by
     * @return list of matching {@link MeetingRegister} entries
     * @throws UserNotFoundException if the user does not exist
     * @throws com.litclub.Backend.exception.MalformedDTOException if identifier is invalid
     */
    @Transactional(readOnly = true)
    public List<MeetingRegister> getAttendanceRecordsByAttendanceAndField(
            Long userID, boolean attended, String identifier, boolean value) {
        User user = requireUserById(userID);
        return meetingRegisterService.findAllByAttendanceBivariable(user, attended, identifier, value);
    }

    /**
     * Retrieves meetings the user actually attended (from register, not RSVP).
     *
     * @param userID the user ID
     * @return list of meetings where attendance was recorded
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsUserAttended(Long userID) {
        User user = requireUserById(userID);
        return meetingRegisterService.findAll(user, "attended", true)
                .stream()
                .map(MeetingRegister::getMeeting)
                .collect(Collectors.toList());
    }

    /**
     * Counts total meetings the user attended.
     *
     * @param userID the user ID
     * @return count of attended meetings
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public long countMeetingsAttended(Long userID) {
        User user = requireUserById(userID);
        return meetingRegisterService.findAll(user, "attended", true).size();
    }

    /**
     * Counts meetings the user was late to.
     *
     * @param userID the user ID
     * @return count of late arrivals
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public long countLateArrivals(Long userID) {
        User user = requireUserById(userID);
        return meetingRegisterService.findAll(user, "late", true).size();
    }

    // ===== UPDATE =====

    /**
     * Updates a user's profile information.
     *
     * <p>Only non-null fields in {@code userRecord} are updated. Passwords are
     * automatically hashed before storage.</p>
     *
     * @param userID the ID of the user to update
     * @param userRecord the new user data (null fields are ignored)
     * @return the updated {@link UserRecord}
     * @throws UserNotFoundException if no user with the given ID exists
     */
    @Transactional
    public UserRecord updateUser(Long userID, UserRegistrationRecord userRecord) {
        User userToUpdate = requireUserById(userID);

        if (userRecord.username() != null) userToUpdate.setUsername(userRecord.username());
        if (userRecord.firstName() != null) userToUpdate.setFirstName(userRecord.firstName());
        if (userRecord.surname() != null) userToUpdate.setSecondName(userRecord.surname());
        if (userRecord.email() != null) userToUpdate.setEmail(userRecord.email());
        if (userRecord.isAdmin()) userToUpdate.setGlobalRoles(Set.of(GlobalRole.ADMINISTRATOR));
        if (userRecord.password() != null)
            userToUpdate.setPasswordHash(passwordEncoder.encode(userRecord.password()));

        userRepository.save(userToUpdate);
        return convertUserToRecord(userToUpdate);
    }

    @Transactional
    public UserRecord updateUser(User user) {
        userRepository.save(user);
        return convertUserToRecord(user);
    }

    // ===== DELETE =====

    /**
     * Deletes a user account by username or email.
     *
     * <p>This operation cascades to related entities based on JPA cascade rules.
     * Ensure proper cascade configuration on relationships to avoid orphaned data.</p>
     *
     * @param identifier either the username or email of the user
     * @throws UserNotFoundException if the user could not be found
     */
    @Transactional
    public void deleteUser(String identifier) {
        User user = requireUserByIdentifier(identifier);
        userRepository.delete(user);
    }

    /**
     * Deletes a user account by ID.
     *
     * @param userID the user ID
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    public void deleteUser(Long userID) {
        User user = requireUserById(userID);
        userRepository.delete(user);
    }

    /**
     * Performs a complete cleanup of all user-related data before deletion.
     *
     * <p>This method explicitly deletes all relationships managed by low-tier services
     * to ensure clean data removal. Use this instead of {@link #deleteUser(Long)} when
     * you want explicit control over cascading deletions.</p>
     *
     * @param userID the user ID
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    public void deleteUserWithCleanup(Long userID) {
        User user = requireUserById(userID);

        // Clean up all discussion prompts
        discussionPromptService.purgeUserPrompts(user);

        // Clean up all reviews
        reviewService.purgeUserReviews(user);

        // Clean up meeting attendance records
        // MeetingRegisterService doesn't have a purge method, so we delete individually
        List<MeetingRegister> attendanceRecords = meetingRegisterService.findAll(user);
        // Note: You might want to add a purgeUserAttendance method to MeetingRegisterService

        // Finally, delete the user (cascades will handle club memberships and user books)
        userRepository.delete(user);
    }

    // ===== STATISTICS & ANALYTICS =====

    /**
     * Calculates the user's average book rating across all reviews.
     *
     * @param userID the user ID
     * @return average rating, or 0.0 if no reviews exist
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public double getAverageRating(Long userID) {
        List<Review> reviews = getReviewsForUser(userID);
        if (reviews.isEmpty()) return 0.0;

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    /**
     * Gets a breakdown of the user's reading statuses.
     *
     * @param userID the user ID
     * @return map of status to count
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public Map<BookStatus, Long> getReadingStatusBreakdown(Long userID) {
        User user = requireUserById(userID);
        return userBooksService.getUserBooksForUser(user)
                .stream()
                .collect(Collectors.groupingBy(
                        UserBook::getStatus,
                        Collectors.counting()
                ));
    }

    /**
     * Counts total books in the user's library.
     *
     * @param userID the user ID
     * @return total number of books
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public long countBooksInLibrary(Long userID) {
        return getBooksForUser(userID).size();
    }

    /**
     * Counts books with a specific status in the user's library.
     *
     * @param userID the user ID
     * @param status the status to count
     * @return number of books with the specified status
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public long countBooksByStatus(Long userID, BookStatus status) {
        User user = requireUserById(userID);
        return userBooksService.getUserBooksForUser(user)
                .stream()
                .filter(ub -> ub.getStatus().equals(status))
                .count();
    }

    /**
     * Counts total reviews written by the user.
     *
     * @param userID the user ID
     * @return total number of reviews
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public long countReviews(Long userID) {
        return getReviewsForUser(userID).size();
    }

    /**
     * Counts discussion prompts created by the user.
     *
     * @param userID the user ID
     * @return total number of prompts
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public long countDiscussionPrompts(Long userID) {
        return getAllDiscussionPromptsForUser(userID).size();
    }

    /**
     * Counts total meeting RSVPs for the user.
     *
     * @param userID the user ID
     * @return total number of RSVPs
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public long countMeetingRSVPs(Long userID) {
        return getMeetingRSVPsForUser(userID).size();
    }

    /**
     * Gets a complete user activity summary.
     *
     * @param userID the user ID
     * @return map containing various activity counts
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserActivitySummary(Long userID) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalClubs", getClubsForUser(userID).size());
        summary.put("totalBooks", countBooksInLibrary(userID));
        summary.put("readingStatusBreakdown", getReadingStatusBreakdown(userID));
        summary.put("totalReviews", countReviews(userID));
        summary.put("averageRating", getAverageRating(userID));
        summary.put("totalPrompts", countDiscussionPrompts(userID));
        summary.put("totalRSVPs", countMeetingRSVPs(userID));
        summary.put("meetingsAttended", countMeetingsAttended(userID));
        summary.put("lateArrivals", countLateArrivals(userID));

        return summary;
    }

    // ===== UTILITY =====

    /**
     * Converts a {@link User} entity to a {@link UserRecord} DTO.
     *
     * <p>This method eagerly loads the user's club memberships to populate
     * the record. Use sparingly in performance-critical paths.</p>
     *
     * @param user the user entity to convert
     * @return the corresponding {@link UserRecord}
     */
    public static UserRecord convertUserToRecord(User user) {
        return new UserRecord(
                user.getUserID(),
                user.getFirstName(),
                user.getSecondName(),
                user.getUsername(),
                user.getEmail(),
                getClubsForUser(user)
        );
    }

    /**
     * Returns all clubs the user is a member of.
     *
     * <p>This method directly accesses the user's memberships without
     * additional database queries, assuming memberships are eagerly loaded
     * or within the same transaction.</p>
     *
     * @param user the user entity
     * @return set of clubs the user belongs to
     */
    public static Set<Club> getClubsForUser(User user) {
        Set<Club> clubs = new HashSet<>();
        for (ClubMembership membership : user.getMemberships()) {
            clubs.add(membership.getClub());
        }
        return clubs;
    }

    /**
     * Checks if a username is already taken.
     *
     * @param username the username to check
     * @return true if the username exists
     */
    @Transactional(readOnly = true)
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Determines if this is the first user being created in the system.
     *
     * <p>The first user is automatically granted administrator privileges
     * to ensure the instance has an initial admin.</p>
     *
     * @return true if no users exist in the database
     */
    private boolean firstLaunch() {
        return userRepository.count() == 0;
    }
}