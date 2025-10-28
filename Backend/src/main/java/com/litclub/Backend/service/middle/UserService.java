package com.litclub.Backend.service.middle;

import com.litclub.Backend.construct.user.UserLoginRecord;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.construct.user.UserRegistrationRecord;
import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.ClubMembership;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.exception.CredentialsAlreadyTakenException;
import com.litclub.Backend.exception.UserNotFoundException;
import com.litclub.Backend.repository.UserRepository;
import com.litclub.Backend.security.jwt.JwtService;
import com.litclub.Backend.security.roles.GlobalRole;
import com.litclub.Backend.service.low.ClubMembershipService;
import com.litclub.Backend.service.low.UserBooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service that manages user authentication, registration, and account maintenance.
 *
 * <p>This service acts as the core entry point for all user-related business logic,
 * including credential validation, persistence, and role assignment. This is a
 * middle tier Service meaning the caller <strong>must enforce access control;
 * this Service does not enforce it</strong></p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Authenticate users by username or email</li>
 *   <li>Register new users with secure password hashing</li>
 *   <li>Update or delete user accounts</li>
 *   <li>Retrieve associated clubs and memberships</li>
 * </ul>
 *
 * @see User
 * @see UserRepository
 * @see JwtService
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final ClubMembershipService clubMembershipService;
    private final UserBooksService userBooksService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ClubMembershipService clubMembershipService,
                       UserBooksService userBooksService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clubMembershipService = clubMembershipService;
        this.userBooksService = userBooksService;
    }

    // ===== AUTHENTICATION =====

    /**
     * Authenticates a user by username or email and returns a {@link UserRecord}.
     *
     * @param userLoginRecord the login credentials containing username/email and password
     * @return the authenticated {@link UserRecord}
     * @throws BadCredentialsException if the credentials are invalid
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
     * @throws BadCredentialsException if the username or password is invalid
     */
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
     * @throws BadCredentialsException if the email or password is invalid
     */
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
     * Registers a new user and hashes their password securely.
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

    /** Retrieves a user by username. */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    /** Retrieves a user by email. */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    /** Retrieves a user by database ID. */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findUserByUserID(id);
    }

    // ====> Clubs
    /** Retrieves a user's clubs
     * Callers must pass either the UserID or the username*/
    @Transactional(readOnly = true)
    public List<Club> getClubsForUser(Long userID) {
        User user = getUserById(userID)
                .orElseThrow(() -> new UserNotFoundException("ID", userID.toString()));
        return clubMembershipService.getClubsForUser(user);
    }

    @Transactional(readOnly = true)
    public List<Club> getClubsForUser(String identifier) {
        User user = getUserByUsername(identifier)
                .or(() -> getUserByEmail(identifier))
                .orElseThrow(() -> new UserNotFoundException("Username/Email", identifier));
        return clubMembershipService.getClubsForUser(user);
    }

    // ====> Books
    @Transactional(readOnly = true)
    public List<Book> getBooksForUser(Long userID) {
        User user = getUserById(userID)
                .orElseThrow(() -> new UserNotFoundException("ID", userID.toString()));
        return userBooksService.getBooksForUser(user);
    }

    @Transactional(readOnly = true)
    public List<Book> getBooksForUser(String identifier) {
        User user = getUserByUsername(identifier)
                .or(() -> getUserByEmail(identifier))
                .orElseThrow(() -> new UserNotFoundException("Username/Email", identifier));
        return userBooksService.getBooksForUser(user);
    }

    // ===== UPDATE =====

    /**
     * Updates a user’s details.
     *
     * @param userID the ID of the user to update
     * @param userRecord the new user data
     * @return the updated {@link UserRecord}
     * @throws UserNotFoundException if no user with the given ID exists
     */
    @Transactional
    public UserRecord updateUser(Long userID, UserRegistrationRecord userRecord) throws UserNotFoundException {
        Optional<User> user = getUserById(userID);

        if (user.isEmpty()) {
            throw new UserNotFoundException("userID", userID.toString());
        }

        User userToUpdate = user.get();

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
     * Deletes a user by username or email.
     *
     * @param identifier either the username or email of the user
     * @throws UserNotFoundException if the user could not be found
     */
    @Transactional
    public void deleteUser(String identifier) throws UserNotFoundException {
        Optional<User> userOpt = userRepository.findUserByUsername(identifier);

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findUserByEmail(identifier);
        }

        User user = userOpt.orElseThrow(() -> new UserNotFoundException("username or email", identifier));
        userRepository.delete(user);
    }

    // ===== UTILITY =====

    /** Converts a {@link User} entity to a {@link UserRecord}. */
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

    /** Returns all clubs the user is a member of. */
    public Set<Club> getClubsForUser(User user) {
        Set<Club> clubs = new HashSet<>();
        for (ClubMembership membership : user.getMemberships()) {
            clubs.add(membership.getClub());
        }
        return clubs;
    }

    /** Returns true if this is the first user created in the system. */
    private boolean firstLaunch() {
        return userRepository.count() == 0;
    }
}
