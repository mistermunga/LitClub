package com.litclub.Backend.service.middle;

import com.litclub.Backend.construct.user.UserLoginRecord;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.construct.user.UserRegistrationRecord;
import com.litclub.Backend.entity.*;
import com.litclub.Backend.exception.CredentialsAlreadyTakenException;
import com.litclub.Backend.exception.UserNotFoundException;
import com.litclub.Backend.repository.UserRepository;
import com.litclub.Backend.security.roles.GlobalRole;
import com.litclub.Backend.service.low.ClubMembershipService;
import com.litclub.Backend.service.low.DiscussionPromptService;
import com.litclub.Backend.service.low.UserBooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Middle-tier service managing user authentication, registration, and account operations.
 *
 * <p>This service orchestrates user-related business logic by delegating to low-tier services
 * for relationship management (clubs, books, prompts). It validates user existence and credentials
 * but <strong>does not enforce access control</strong> — callers are responsible for authorization.</p>
 *
 * <p><strong>Design Principles:</strong></p>
 * <ul>
 *   <li><strong>Input Validation:</strong> All public methods validate that users exist before proceeding</li>
 *   <li><strong>Entity Trust:</strong> When entities (Club, Book) are passed in, they are assumed valid</li>
 *   <li><strong>No Access Control:</strong> Callers must enforce permissions via top-tier services</li>
 *   <li><strong>Delegation:</strong> Relationship operations are delegated to low-tier services</li>
 * </ul>
 *
 * <p><strong>Tier Position:</strong> Middle tier — depends only on low-tier services and repositories.</p>
 *
 * @see User
 * @see UserRepository
 * @see ClubMembershipService
 * @see UserBooksService
 * @see DiscussionPromptService
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClubMembershipService clubMembershipService;
    private final UserBooksService userBooksService;
    private final DiscussionPromptService discussionPromptService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ClubMembershipService clubMembershipService,
                       UserBooksService userBooksService,
                       DiscussionPromptService discussionPromptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clubMembershipService = clubMembershipService;
        this.userBooksService = userBooksService;
        this.discussionPromptService = discussionPromptService;
    }

    // ===== AUTHENTICATION =====

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

    // ===== CLUBS =====

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

    // ===== BOOKS =====

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
    public void removeBookFromPersonalLibrary(Long userID, Book book) {
        User user = requireUserById(userID);
        userBooksService.removeUserBook(user, book);
    }

    // ===== DISCUSSION PROMPTS =====

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
    public List<DiscussionPrompt> getAllDiscussionPromptsForUserAndClub(Long userID, Club club) {
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
    public void purgePrompts(Long userID) {
        User user = requireUserById(userID);
        discussionPromptService.purgeUserPrompts(user);
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
    public UserRecord convertUserToRecord(User user) {
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
    public Set<Club> getClubsForUser(User user) {
        Set<Club> clubs = new HashSet<>();
        for (ClubMembership membership : user.getMemberships()) {
            clubs.add(membership.getClub());
        }
        return clubs;
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