package com.litclub.Backend.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Singleton service for managing instance-wide and club-specific configuration.
 *
 * <p>Configuration is persisted as a JSON file ({@code instance-config.json}) in the
 * application data directory and is loaded into memory for fast, concurrent access.
 * Access to the in-memory configuration is guarded by a read/write lock to allow
 * concurrent reads and serialized writes.
 *
 * <p>This component is a Spring {@link Component} and is initialized after construction
 * via {@link #initialize()} (annotated with {@code @PostConstruct}).
 *
 * @see #getConfigFilePath()
 * @see InstanceSettings
 * @see ClubFlags
 */
@SuppressWarnings("ALL")
@Component
public class ConfigurationManager {

    private static final String CONFIG_FILENAME = "instance-config.json";
    private final Path configFilePath;
    private final ObjectMapper objectMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private InstanceConfiguration configuration;

    /**
     * Constructs a new {@code ConfigurationManager} and resolves the path to the
     * configuration file.
     *
     * <p>The constructor sets up the {@link ObjectMapper} with pretty-printing
     * and case-insensitive enum handling.
     *
     * @throws IOException if the configuration directory cannot be resolved/created
     */
    public ConfigurationManager() throws IOException {
        this.configFilePath = resolveConfigPath();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    /**
     * Post-construction initializer invoked by Spring.
     *
     * <p>This method attempts to load the configuration from disk. If the file does not
     * exist, a default configuration is created and persisted. If the file exists but
     * cannot be parsed, the corrupted file is backed up and defaults are created.
     *
     * @throws RuntimeException if configuration initialization ultimately fails
     */
    @PostConstruct
    public void initialize() {
        try {
            loadConfiguration();
            System.out.println("✓ Configuration loaded from: " + configFilePath);
        } catch (IOException e) {
            System.err.println("Failed to initialize ConfigurationManager: " + e.getMessage());
            throw new RuntimeException("Configuration initialization failed", e);
        }
    }

    // ====== INSTANCE-LEVEL CONFIGURATION ======

    /**
     * Returns the configured {@link InstanceRegistrationMode}.
     *
     * @return the instance registration mode (never {@code null})
     */
    public InstanceRegistrationMode getRegistrationMode() {
        lock.readLock().lock();
        try {
            return configuration.instance.registrationMode;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the configured {@link ClubCreationMode}.
     *
     * @return the club creation mode (never {@code null})
     */
    public ClubCreationMode getClubCreationMode() {
        lock.readLock().lock();
        try {
            return configuration.instance.clubCreationMode;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the configured maximum number of clubs a single user may create/join.
     *
     * @return the maximum clubs per user
     */
    public int getMaxClubsPerUser() {
        lock.readLock().lock();
        try {
            return configuration.instance.maxClubsPerUser;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the configured maximum number of members allowed in a single club.
     *
     * @return the maximum members per club
     */
    public int getMaxMembersPerClub() {
        lock.readLock().lock();
        try {
            return configuration.instance.maxMembersPerClub;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns a snapshot of instance-level settings.
     *
     * <p>The returned {@link InstanceSettings} is a simple immutable record representing
     * current instance-level values.
     *
     * @return the current instance settings
     */
    public InstanceSettings getInstanceSettings() {
        lock.readLock().lock();
        try {
            return new InstanceSettings(
                    configuration.instance.registrationMode,
                    configuration.instance.clubCreationMode,
                    configuration.instance.maxClubsPerUser,
                    configuration.instance.maxMembersPerClub
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Updates instance-wide settings and persists the configuration to disk.
     *
     * <p>This method acquires a write lock for the duration of the update and persist.
     * It is intended to be called by administrators only.
     *
     * @param settings the new settings to apply
     * @throws IOException if persisting the updated configuration fails
     */
    public void updateInstanceSettings(InstanceSettings settings) throws IOException {
        lock.writeLock().lock();
        try {
            configuration.instance.registrationMode = settings.registrationMode();
            configuration.instance.clubCreationMode = settings.clubCreationMode();
            configuration.instance.maxClubsPerUser = settings.maxClubsPerUser();
            configuration.instance.maxMembersPerClub = settings.maxMembersPerClub();

            persistConfiguration();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ====== CLUB-LEVEL CONFIGURATION ======

    /**
     * Returns the {@link ClubFlags} for a specific club.
     *
     * <p>If the club has no explicit configuration, default flags are returned via
     * {@link ClubFlags#defaults()}.
     *
     * @param clubId the id of the club
     * @return the club flags (never {@code null})
     */
    public ClubFlags getClubFlags(Long clubId) {
        lock.readLock().lock();
        try {
            String key = clubId.toString();
            if (configuration.clubs.containsKey(key)) {
                ClubFlagsData data = configuration.clubs.get(key);
                return new ClubFlags(
                        data.allowPublicNotes,
                        data.requireMeetingRSVP,
                        data.allowMemberInvites,
                        data.allowMemberDiscussion,
                        data.enableRegister
                );
            }
            // Return defaults for unconfigured clubs
            return ClubFlags.defaults();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Creates or updates club-specific flags and persists the change.
     *
     * @param clubId the id of the club to update
     * @param flags the new flags to apply
     * @throws IOException if persisting the updated configuration fails
     */
    public void setClubFlags(Long clubId, ClubFlags flags) throws IOException {
        lock.writeLock().lock();
        try {
            String key = clubId.toString();
            ClubFlagsData data = new ClubFlagsData();
            data.allowPublicNotes = flags.allowPublicNotes();
            data.requireMeetingRSVP = flags.requireMeetingRSVP();
            data.allowMemberInvites = flags.allowMemberInvites();
            data.allowMemberDiscussion = flags.allowMemberDiscussion();
            data.enableRegister = flags.enableRegister();

            configuration.clubs.put(key, data);
            persistConfiguration();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Initializes default flags for a newly created club.
     *
     * @param clubId the id of the new club
     * @throws IOException if persisting the defaults fails
     */
    public void initializeClubFlags(Long clubId) throws IOException {
        setClubFlags(clubId, ClubFlags.defaults());
    }

    /**
     * Removes stored configuration for a deleted club and persists the change.
     *
     * @param clubId the id of the club to remove
     * @throws IOException if persisting the change fails
     */
    public void removeClubFlags(Long clubId) throws IOException {
        lock.writeLock().lock();
        try {
            configuration.clubs.remove(clubId.toString());
            persistConfiguration();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ====== FILE OPERATIONS ======

    /**
     * Loads the configuration from disk into memory.
     *
     * <p>If the config file does not exist, this method creates a default configuration
     * and persists it. If the file exists but cannot be parsed, the corrupted file
     * is backed up and defaults are created.
     *
     * @throws IOException if reading from disk or creating defaults fails
     */
    private void loadConfiguration() throws IOException {
        if (!Files.exists(configFilePath)) {
            System.out.println("Config file not found. Creating with defaults...");
            createDefaultConfiguration();
            return;
        }

        try {
            String json = Files.readString(configFilePath);
            configuration = objectMapper.readValue(json, InstanceConfiguration.class);

            // Defensive: ensure sub-structures exist for older/corrupted configs
            if (configuration.instance == null) {
                configuration.instance = new InstanceData();
            }
            if (configuration.clubs == null) {
                configuration.clubs = new HashMap<>();
            }
        } catch (IOException e) {
            System.err.println("Failed to parse config file. Creating backup and using defaults.");
            backupCorruptedFile();
            createDefaultConfiguration();
        }
    }

    /**
     * Persists the in-memory configuration to the config file.
     *
     * @throws IOException if writing to disk fails
     */
    private void persistConfiguration() throws IOException {
        String json = objectMapper.writeValueAsString(configuration);
        Files.writeString(configFilePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Creates a default {@link InstanceConfiguration}, persists it, and attempts to
     * set restrictive file permissions.
     *
     * @throws IOException if persisting the default configuration fails
     */
    private void createDefaultConfiguration() throws IOException {
        configuration = new InstanceConfiguration();
        configuration.instance = new InstanceData();
        configuration.instance.registrationMode = InstanceRegistrationMode.INVITE_ONLY;
        configuration.instance.clubCreationMode = ClubCreationMode.APPROVAL_REQUIRED;
        configuration.instance.maxClubsPerUser = 5;
        configuration.instance.maxMembersPerClub = 50;
        configuration.clubs = new HashMap<>();

        persistConfiguration();
        setRestrictivePermissions();
    }

    /**
     * Attempts to set POSIX file permissions to owner read/write only (i.e. 600).
     *
     * <p>If the underlying filesystem does not support POSIX attributes or setting
     * permissions fails, the exception is logged and ignored.
     */
    private void setRestrictivePermissions() {
        try {
            if (configFilePath.getFileSystem().supportedFileAttributeViews().contains("posix")) {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                Files.setPosixFilePermissions(configFilePath, perms);
                System.out.println("✓ Set restrictive permissions (600) on config file");
            }
        } catch (IOException e) {
            System.err.println("Could not set file permissions: " + e.getMessage());
        }
    }

    /**
     * Moves the corrupted config file to a timestamped backup file next to the config.
     *
     * @throws IOException if the backup operation fails
     */
    private void backupCorruptedFile() throws IOException {
        Path backup = configFilePath.resolveSibling(CONFIG_FILENAME + ".corrupted." + System.currentTimeMillis());
        Files.move(configFilePath, backup, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Corrupted config backed up to: " + backup);
    }

    /**
     * Resolves the path to the configuration file based on the running OS.
     *
     * <p>On Windows the config directory is {@code %APPDATA%/LitClub}, on macOS it is
     * {@code ~/Library/Application Support/LitClub}, and on other systems it is
     * {@code ~/.litclub}. The directory is created if it does not already exist.
     *
     * @return the {@link Path} to the instance configuration file
     * @throws IOException if creating the configuration directory fails
     */
    private Path resolveConfigPath() throws IOException {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        Path configDir;
        if (os.contains("win")) {
            configDir = Paths.get(System.getenv("APPDATA"), "LitClub");
        } else if (os.contains("mac")) {
            configDir = Paths.get(userHome, "Library", "Application Support", "LitClub");
        } else {
            configDir = Paths.get(userHome, ".litclub");
        }

        Files.createDirectories(configDir);
        return configDir.resolve(CONFIG_FILENAME);
    }

    /**
     * Returns the resolved {@link Path} to the configuration file used by this manager.
     *
     * @return the configuration file path
     */
    public Path getConfigFilePath() {
        return configFilePath;
    }

    // ====== DATA CLASSES ======

    private static class InstanceConfiguration {
        public InstanceData instance;
        public Map<String, ClubFlagsData> clubs;
    }

    private static class InstanceData {
        public InstanceRegistrationMode registrationMode;
        public ClubCreationMode clubCreationMode;
        public int maxClubsPerUser;
        public int maxMembersPerClub;
    }

    private static class ClubFlagsData {
        public boolean allowPublicNotes;
        public boolean requireMeetingRSVP;
        public boolean allowMemberInvites;
        public boolean allowMemberDiscussion;
        public boolean enableRegister;
    }

    // ====== PUBLIC RECORDS ======

    /**
     * Instance-wide configuration settings snapshot.
     *
     * @param registrationMode the current {@link InstanceRegistrationMode}
     * @param clubCreationMode the current {@link ClubCreationMode}
     * @param maxClubsPerUser maximum clubs a user may create/join
     * @param maxMembersPerClub maximum members allowed per club
     */
    public record InstanceSettings(
            InstanceRegistrationMode registrationMode,
            ClubCreationMode clubCreationMode,
            int maxClubsPerUser,
            int maxMembersPerClub
    ) {}

    /**
     * Club-specific configuration flags.
     *
     * @param allowPublicNotes If {@code true}, members can create notes shared with the club.
     * @param requireMeetingRSVP If {@code true}, members must RSVP to meetings.
     * @param allowMemberInvites If {@code true}, regular members may invite others (not only admins).
     * @param allowMemberDiscussion If {@code true}, regular members can create discussion prompts
     * @param enableRegister If {@code true}, {@link com.litclub.Backend.entity.MeetingRegister} functionality enabled
     */
    public record ClubFlags(
            boolean allowPublicNotes,
            boolean requireMeetingRSVP,
            boolean allowMemberInvites,
            boolean allowMemberDiscussion,
            boolean enableRegister
    ) {
        /**
         * Returns the default flags used for newly-created clubs.
         *
         * @return the default {@link ClubFlags}
         */
        public static ClubFlags defaults() {
            return new ClubFlags(true, false, true, true, true);
        }
    }

    /**
     * Defines the policy for how users may join the instance.
     *
     * <p>Default is {@link #INVITE_ONLY}.
     */
    public enum InstanceRegistrationMode {
        OPEN, INVITE_ONLY, CLOSED
    }

    /**
     * Defines policies controlling how clubs may be created.
     */
    public enum ClubCreationMode {
        FREE, APPROVAL_REQUIRED, ADMIN_ONLY
    }
}
