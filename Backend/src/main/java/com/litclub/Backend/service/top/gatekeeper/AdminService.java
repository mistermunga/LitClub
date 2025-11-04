package com.litclub.Backend.service.top.gatekeeper;

import com.litclub.Backend.config.ConfigurationManager;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.construct.user.UserRegistrationRecord;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.security.roles.GlobalRole;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Top-tier gatekeeper service that exposes administrative operations for the application.
 *
 * <p><strong>Purpose</strong></p>
 * <p>
 * AdminService is a top-tier (controller-facing) service whose primary responsibilities are:
 * <ul>
 *   <li>Enforcing access control for administrative operations (class-level {@code @PreAuthorize} ensures only administrators may call its methods).</li>
 *   <li>Acting as a gatekeeper and orchestrator: validate input / existence when appropriate, then delegate to middle-tier services
 *       ({@link UserService}, {@link ClubService}) and to the {@link ConfigurationManager} to perform the actual work.</li>
 *   <li>Providing a clear API for controllers to perform system-level tasks such as promoting users, creating/deleting users and clubs,
 *       and reading/updating instance configuration.</li>
 * </ul>
 * </p>
 *
 * <p><strong>Design notes / guarantees</strong></p>
 * <ul>
 *   <li><strong>Access control:</strong> Access is enforced at the class level by the annotation {@code @PreAuthorize("@userSecurity.isAdmin(authentication)")}
 *       so only authenticated principals recognized as administrators may invoke these methods. Individual methods do not perform additional authorization checks.</li>
 *   <li><strong>Delegation:</strong> This service delegates business logic and persistence to the middle-tier services:
 *       {@link UserService} for user management, {@link ClubService} for club management, and {@link ConfigurationManager} for system configuration.</li>
 *   <li><strong>Input / existence validation:</strong> Calls that require an existing entity (for example promoting a user) rely on the middle-tier
 *       method {@code requireUserById} to validate existence and fail fast if not present.</li>
 *   <li><strong>Side effects & exceptions:</strong> Methods that mutate persistent configuration may throw {@link java.io.IOException} when the
 *       configuration subsystem cannot persist changes.</li>
 *   <li><strong>Thread-safety:</strong> The service itself is stateless (aside from final injected collaborators) and is safe for use as a Spring singleton;
 *       thread-safety of operations depends on the underlying services and repositories.</li>
 * </ul>
 *
 * <p><strong>Tier position</strong></p>
 * <p>Top tier / Gatekeeper service — depends on middle-tier services ({@link UserService}, {@link ClubService}) and {@link ConfigurationManager}.</p>
 *
 * @see UserService
 * @see ClubService
 * @see ConfigurationManager
 * @see com.litclub.Backend.construct.user.UserRecord
 * @see com.litclub.Backend.construct.user.UserRegistrationRecord
 * @see com.litclub.Backend.entity.User
 * @see com.litclub.Backend.entity.Club
 */
@Service
@PreAuthorize("@userSecurity.isAdmin(authentication)")
public class AdminService {

    private final UserService userService;
    private final ClubService clubService;
    private final ConfigurationManager configuration;
    /**
     * Create a new AdminService.
     *
     * <p>Dependencies are injected and held as final collaborators. This class is intended to be used as a Spring singleton
     * and performs no heavy local state management.</p>
     *
     * @param userService    middle-tier service responsible for user operations (register, update, delete, fetch).
     * @param clubService    middle-tier service responsible for club operations (delete, list, etc.).
     * @param configuration  configuration manager that holds and persists instance-wide settings.
     */
    public AdminService(
            UserService userService,
            ClubService clubService,
            ConfigurationManager configuration
    ) {
        this.userService = userService;
        this.clubService = clubService;
        this.configuration = configuration;
    }

    // ====== USERS ======

    /**
     * Promote the user identified by {@code userID} to a global administrator.
     *
     * <p>This method:
     * <ol>
     *   <li>Requires the user to exist by calling {@link UserService#requireUserById(Long)} (which will typically throw an exception if not found).</li>
     *   <li>Adds the {@link com.litclub.Backend.security.roles.GlobalRole#ADMINISTRATOR} role to the user's global role set.</li>
     *   <li>Persists the change by delegating to {@link UserService#updateUser(User)} and returns the updated {@link com.litclub.Backend.construct.user.UserRecord}.</li>
     * </ol>
     * </p>
     *
     * <p><strong>Notes:</strong> This method mutates the {@link com.litclub.Backend.entity.User#getGlobalRoles()} set in-place. It assumes that
     * the returned set is mutable; if underlying implementation returns an immutable set, the update will need to create a defensive copy.</p>
     *
     * @param userID the id of the user to promote (must not be {@code null}).
     * @return the updated {@link com.litclub.Backend.construct.user.UserRecord} representing the promoted user.
     * @throws java.lang.IllegalArgumentException if {@code userID} is {@code null} (delegated methods may throw other runtime exceptions if user not found).
     * @see UserService#requireUserById(Long)
     * @see UserService#updateUser(com.litclub.Backend.entity.User)
     */
    @Transactional
    public UserRecord promoteAdmin(Long userID) {
        User user = userService.requireUserById(userID);
        Set<GlobalRole> roles = user.getGlobalRoles();
        roles.add(GlobalRole.ADMINISTRATOR);
        user.setGlobalRoles(roles);
        return userService.updateUser(user);
    }

    /**
     * Change the instance-wide registration policy.
     *
     * <p>This method loads the current {@link ConfigurationManager.InstanceSettings}, creates a new
     * {@code InstanceSettings} preserving the existing club creation mode and limits, but replacing the
     * registration mode with the provided {@code mode}, and then persists the new settings via
     * {@link ConfigurationManager#updateInstanceSettings(ConfigurationManager.InstanceSettings)}.</p>
     *
     * <p><strong>Important:</strong> Persisting configuration may fail due to I/O problems; callers should handle {@link java.io.IOException}.</p>
     *
     * @param mode the new instance registration mode to set (must not be {@code null}).
     * @throws java.io.IOException if persisting the new instance settings fails.
     * @see ConfigurationManager#getInstanceSettings()
     * @see ConfigurationManager#updateInstanceSettings(ConfigurationManager.InstanceSettings)
     */
    @Transactional
    public void changeRegistrationPolicy(ConfigurationManager.InstanceRegistrationMode mode) throws IOException {
        ConfigurationManager.InstanceSettings old = configuration.getInstanceSettings();

        configuration.updateInstanceSettings(
                new ConfigurationManager.InstanceSettings(
                        mode,
                        old.clubCreationMode(),
                        old.maxClubsPerUser(),
                        old.maxMembersPerClub()
                )
        );
    }

    /**
     * Create (register) a new user using the provided registration record.
     *
     * <p>Delegates to {@link UserService#registerUser(UserRegistrationRecord)} for validation, persistence, and
     * any additional business rules (e.g. email uniqueness, password hashing).</p>
     *
     * @param userRegistrationRecord data required to create a new user (must not be {@code null}).
     * @return a {@link com.litclub.Backend.construct.user.UserRecord} representing the newly created user.
     * @throws java.lang.IllegalArgumentException if {@code userRegistrationRecord} is {@code null} (or if required fields are missing — delegated to {@link UserService}).
     * @see UserService#registerUser(UserRegistrationRecord)
     */
    @Transactional
    public UserRecord createUser(UserRegistrationRecord userRegistrationRecord) {
        return userService.registerUser(userRegistrationRecord);
    }

    /**
     * Delete a user and all user-related data as handled by {@link UserService}.
     *
     * <p>Delegates to {@link UserService#deleteUser(Long)}. The exact behavior (cascade, soft delete, checks)
     * is determined by {@link UserService} and underlying repositories.</p>
     *
     * @param userID id of the user to delete (must not be {@code null}).
     * @see UserService#deleteUser(Long)
     */
    @Transactional
    public void deleteUser(Long userID) {
        userService.deleteUser(userID);
    }

    /**
     * Retrieve a list of all users in the system as {@link com.litclub.Backend.construct.user.UserRecord} objects.
     *
     * <p>This method:
     * <ol>
     *   <li>Calls {@link UserService#getAllUsers()} to obtain the raw {@link com.litclub.Backend.entity.User} list.</li>
     *   <li>Converts each {@code User} to a {@code UserRecord} via {@link UserService#convertUserToRecord(User)}.</li>
     * </ol>
     * </p>
     *
     * <p>Returned list is a new {@link java.util.List} (stream-mapped), safe for callers to read/iterate without affecting
     * internal service state.</p>
     *
     * @return a {@link java.util.List} of {@link com.litclub.Backend.construct.user.UserRecord}; never {@code null} (may be empty).
     * @see UserService#getAllUsers()
     * @see UserService#convertUserToRecord(User)
     */
    @Transactional
    public List<UserRecord> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return users.stream()
                .map(UserService::convertUserToRecord)
                .collect(Collectors.toList());
    }

    // ====== CLUBS ======

    /**
     * Permanently delete a club identified by {@code clubID}.
     *
     * <p>Delegates to {@link ClubService#deleteClub(Long)}. The ClubService defines cascade rules, membership cleanup,
     * and any domain-specific constraints about deleting clubs (for example, preventing deletion if active meetings exist).</p>
     *
     * @param clubID id of the club to delete (must not be {@code null}).
     * @see ClubService#deleteClub(Long)
     */
    @Transactional
    public void deleteClub(Long clubID) {
        clubService.deleteClub(clubID);
    }

    /**
     * Return all clubs known to the system.
     *
     * <p>This is a thin delegation to {@link ClubService#getClubs(Pageable)} and returns whatever representation that service exposes
     * (a list of {@link com.litclub.Backend.entity.Club} instances).</p>
     *
     * @return a {@link java.util.List} of {@link com.litclub.Backend.entity.Club}; never {@code null} (may be empty).
     * @see ClubService#getClubs(Pageable)
     */
    @Transactional
    public Page<Club> getAllClubs(Pageable pageable) {
        return clubService.getClubs(pageable);
    }

    // ====== SYS CONFIG ======

    /**
     * Retrieve the current instance settings as held by {@link ConfigurationManager}.
     *
     * <p>Use this to read registration mode, club creation mode, and configured limits.</p>
     *
     * @return the current {@link ConfigurationManager.InstanceSettings}.
     * @see ConfigurationManager#getInstanceSettings()
     */
    public ConfigurationManager.InstanceSettings getInstanceSettings() {
        return configuration.getInstanceSettings();
    }

    /**
     * Replace the instance settings with the provided {@code newInstanceSettings} and persist the change.
     *
     * <p>Delegates to {@link ConfigurationManager#updateInstanceSettings(ConfigurationManager.InstanceSettings)}.
     * This may perform validation and may throw {@link java.io.IOException} if persistence fails.</p>
     *
     * @param newInstanceSettings the new instance settings to persist (must not be {@code null}).
     * @throws java.io.IOException if persisting the configuration fails.
     * @see ConfigurationManager#updateInstanceSettings(ConfigurationManager.InstanceSettings)
     */
    public ConfigurationManager.InstanceSettings updateInstanceSettings(ConfigurationManager.InstanceSettings newInstanceSettings) throws IOException {
        configuration.updateInstanceSettings(newInstanceSettings);
        return getInstanceSettings();
    }
}

