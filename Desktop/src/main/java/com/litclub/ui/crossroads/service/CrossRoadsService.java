package com.litclub.ui.crossroads.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.Club;
import com.litclub.construct.Meeting;
import com.litclub.construct.interfaces.club.ClubCreateRequest;
import com.litclub.construct.interfaces.config.ConfigurationManager;
import com.litclub.construct.interfaces.user.UserRecord;
import com.litclub.persistence.repository.ClubRepository;
import com.litclub.persistence.repository.InstanceRepository;
import com.litclub.session.AppSession;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service layer for the CrossRoads page.
 *
 * <p>Handles all data fetching, club operations, and coordination between
 * repositories. Provides callbacks for UI updates with proper error handling.</p>
 */
public class CrossRoadsService {

    private final ClubRepository clubRepository;
    private final InstanceRepository instanceRepository;
    private final AppSession session;

    public CrossRoadsService() {
        this.clubRepository = ClubRepository.getInstance();
        this.instanceRepository = InstanceRepository.getInstance();
        this.session = AppSession.getInstance();
    }

    // ==================== DATA LOADING ====================

    /**
     * Loads all data needed for the CrossRoads page.
     * Fetches user's clubs, meetings, and instance settings.
     *
     * @param onSuccess callback when all data is loaded
     * @param onError callback if any fetch fails
     */
    public void loadAllData(Runnable onSuccess, Consumer<String> onError) {
        UserRecord user = session.getUserRecord();
        if (user == null) {
            Platform.runLater(() -> onError.accept("User session not found"));
            return;
        }

        // Fetch all data in parallel
        CompletableFuture<Void> clubsFuture = clubRepository.fetchUserClubs(user.userID());
        CompletableFuture<Void> meetingsFuture = clubRepository.fetchUserMeetings(user.userID());
        CompletableFuture<?> settingsFuture = instanceRepository.fetchInstanceSettings();

        // Wait for all to complete
        CompletableFuture.allOf(clubsFuture, meetingsFuture, settingsFuture)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("All data loaded. Clubs count: " + clubRepository.getUserClubs().size());
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to load data: " + errorMessage);
                        onError.accept("Failed to load data: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Refreshes only the meetings data.
     *
     * @param onSuccess callback when meetings are refreshed
     * @param onError callback if refresh fails
     */
    public void refreshMeetings(Runnable onSuccess, Consumer<String> onError) {
        UserRecord user = session.getUserRecord();
        if (user == null) {
            Platform.runLater(() -> onError.accept("User session not found"));
            return;
        }

        clubRepository.fetchUserMeetings(user.userID())
                .thenRun(() -> Platform.runLater(onSuccess))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept("Failed to refresh meetings: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Refreshes only the clubs data.
     *
     * @param onSuccess callback when clubs are refreshed
     * @param onError callback if refresh fails
     */
    public void refreshClubs(Runnable onSuccess, Consumer<String> onError) {
        UserRecord user = session.getUserRecord();
        if (user == null) {
            Platform.runLater(() -> onError.accept("User session not found"));
            return;
        }

        clubRepository.fetchUserClubs(user.userID())
                .thenRun(() -> Platform.runLater(onSuccess))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept("Failed to refresh clubs: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Fetches the user's role in a specific club and sets it in AppSession.
     *
     * @param clubID the club's ID
     * @param onSuccess callback when role is fetched
     * @param onError callback if fetch fails
     */
    public void fetchAndSetClubRole(Long clubID, Runnable onSuccess, Consumer<String> onError) {
        clubRepository.fetchClubPermission(clubID)
                .thenRun(() -> Platform.runLater(onSuccess))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept("Failed to fetch club role: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Prepares club context by setting the club and fetching the user's role.
     * This should be called before navigating to a club page.
     *
     * @param club the club to enter
     * @param onSuccess callback when context is ready
     * @param onError callback if preparation fails
     */
    public void prepareClubContext(Club club, Runnable onSuccess, Consumer<String> onError) {
        // Set the club in session
        session.setCurrentClub(club);

        // Fetch the user's role in this club
        fetchAndSetClubRole(club.getClubID(), onSuccess, onError);
    }

    /**
     * Prepares personal context by clearing club data from session.
     */
    public void preparePersonalContext() {
        session.clearClubContext();
    }

    // ==================== CLUB OPERATIONS ====================

    /**
     * Creates a new club.
     *
     * @param clubName club name
     * @param description club description
     * @param onSuccess callback with the created club
     * @param onError callback if creation fails
     */
    public void createClub(String clubName, String description,
                           Consumer<Club> onSuccess, Consumer<String> onError) {

        UserRecord user = session.getUserRecord();
        if (user == null) {
            Platform.runLater(() -> onError.accept("User session not found"));
            return;
        }

        // Validate inputs
        if (clubName == null || clubName.trim().isEmpty()) {
            Platform.runLater(() -> onError.accept("Club name cannot be empty"));
            return;
        }

        if (clubName.length() > 100) {
            Platform.runLater(() -> onError.accept("Club name must be 100 characters or less"));
            return;
        }

        if (description != null && description.length() > 500) {
            Platform.runLater(() -> onError.accept("Description must be 500 characters or less"));
            return;
        }

        // Create request
        ClubCreateRequest request = new ClubCreateRequest(
                clubName.trim(),
                description != null ? description.trim() : "",
                user
        );

        clubRepository.createClub(request)
                .thenAccept(club -> Platform.runLater(() -> {
                    session.setCurrentClub(club);
                    onSuccess.accept(club);
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept("Failed to create club: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Joins an existing club.
     *
     * @param clubID the club's ID
     * @param onSuccess callback when join succeeds
     * @param onError callback if join fails
     */
    public void joinClub(Long clubID, Runnable onSuccess, Consumer<String> onError) {
        clubRepository.joinClub(clubID)
                .thenRun(() -> Platform.runLater(onSuccess))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept("Failed to join club: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Leaves a club.
     *
     * @param clubID the club's ID
     * @param onSuccess callback when leave succeeds
     * @param onError callback if leave fails
     */
    public void leaveClub(Long clubID, Runnable onSuccess, Consumer<String> onError) {
        clubRepository.leaveClub(clubID)
                .thenRun(() -> Platform.runLater(onSuccess))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept("Failed to leave club: " + errorMessage);
                    });
                    return null;
                });
    }

    public CompletableFuture<Void> redeemInvite(
            String invite,
            Runnable onSuccess,
            Consumer<String> onError
    ) {
        UserRecord user = session.getUserRecord();
        if (user == null) {
            Platform.runLater(() -> onError.accept("User session not found"));
            return CompletableFuture.completedFuture(null);
        }

        return clubRepository.redeemInvite(invite)
                .thenCompose(membership -> {

                    // Extract the club ID from the membership
                    Long clubID = membership.getClubMembershipID().getClubID();

                    // Refresh user's clubs
                    return clubRepository.fetchUserClubs(user.userID())
                            .thenApply(v -> clubID); // pass clubID along
                })
                .thenAccept(clubID -> {
                    Platform.runLater(() -> {
                        // Set the current club using existing cached list
                        var club = getClubs().stream()
                                .filter(c -> c.getClubID().equals(clubID))
                                .findFirst()
                                .orElse(null);

                        if (club != null) {
                            session.setCurrentClub(club);
                            onSuccess.run();
                        } else {
                            onError.accept("Invite redeemed but club not found after refresh.");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        String message = ApiErrorHandler.parseError(ex);
                        onError.accept("Failed to redeem invite: " + message);
                    });
                    return null;
                });
    }



    // ==================== OBSERVABLE DATA ACCESS ====================

    /**
     * Gets the observable list of user's clubs.
     * UI components can bind to this for reactive updates.
     *
     * @return unmodifiable observable list of clubs
     */
    public ObservableList<Club> getClubs() {
        return clubRepository.getUserClubs();
    }

    /**
     * Gets the observable list of user's meetings.
     * UI components can bind to this for reactive updates.
     *
     * @return unmodifiable observable list of meetings
     */
    public ObservableList<Meeting> getMeetings() {
        return clubRepository.getUserMeetings();
    }

    // ==================== INSTANCE SETTINGS ====================

    /**
     * Checks if the current user can create clubs based on instance settings.
     *
     * @return true if user can create clubs
     */
    public boolean canCreateClubs() {
        ConfigurationManager.InstanceSettings settings = instanceRepository.getInstanceSettings();
        if (settings == null) {
            return false; // Conservative default if settings not loaded
        }

        return switch (settings.clubCreationMode()) {
            case FREE -> true;
            case ADMIN_ONLY -> session.isAdmin();
            case APPROVAL_REQUIRED -> true; // Can create, but needs approval
        };
    }

    /**
     * Gets the current club creation mode.
     *
     * @return the club creation mode, or null if settings not loaded
     */
    public ConfigurationManager.ClubCreationMode getClubCreationMode() {
        ConfigurationManager.InstanceSettings settings = instanceRepository.getInstanceSettings();
        return settings != null ? settings.clubCreationMode() : null;
    }

    /**
     * Checks if clubs need approval before creation.
     *
     * @return true if approval is required
     */
    public boolean clubsNeedApproval() {
        ConfigurationManager.ClubCreationMode mode = getClubCreationMode();
        return mode == ConfigurationManager.ClubCreationMode.APPROVAL_REQUIRED;
    }

    /**
     * Gets the maximum number of clubs a user can join/create.
     *
     * @return max clubs per user, or -1 if settings not loaded
     */
    public int getMaxClubsPerUser() {
        ConfigurationManager.InstanceSettings settings = instanceRepository.getInstanceSettings();
        return settings != null ? settings.maxClubsPerUser() : -1;
    }

    /**
     * Checks if user has reached the club limit.
     *
     * @return true if user is at or over the club limit
     */
    public boolean hasReachedClubLimit() {
        int maxClubs = getMaxClubsPerUser();
        if (maxClubs <= 0) {
            return false; // No limit or settings not loaded
        }

        return getClubs().size() >= maxClubs;
    }

    // ==================== USER INFO ====================

    /**
     * Gets the current user's record.
     *
     * @return the current user, or null if not logged in
     */
    public UserRecord getCurrentUser() {
        return session.getUserRecord();
    }

    /**
     * Checks if the current user is an administrator.
     *
     * @return true if user is admin
     */
    public boolean isAdmin() {
        return session.isAdmin();
    }

    // ==================== UTILITY ====================

    /**
     * Gets a club by its ID from the loaded clubs.
     *
     * @param clubID the club's ID
     * @return the club, or null if not found
     */
    public Club getClubById(Long clubID) {
        return getClubs().stream()
                .filter(club -> club.getClubID().equals(clubID))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if user is a member of a specific club.
     *
     * @param clubID the club's ID
     * @return true if user is a member
     */
    public boolean isMemberOf(Long clubID) {
        return getClubById(clubID) != null;
    }
}
