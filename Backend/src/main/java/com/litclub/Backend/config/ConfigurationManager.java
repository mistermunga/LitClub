package com.litclub.Backend.config;

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
 * Configuration is stored in a JSON file in the application data directory
 * and loaded into memory for fast access.
 */
@SuppressWarnings("ALL")
@Component
public class ConfigurationManager {

    private static final String CONFIG_FILENAME = "instance-config.json";
    private final Path configFilePath;
    private final ObjectMapper objectMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private InstanceConfiguration configuration;

    public ConfigurationManager() throws IOException {
        this.configFilePath = resolveConfigPath();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

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

    public String getRegistrationMode() {
        lock.readLock().lock();
        try {
            return configuration.instance.registrationMode;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getClubCreationMode() {
        lock.readLock().lock();
        try {
            return configuration.instance.clubCreationMode;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getMaxClubsPerUser() {
        lock.readLock().lock();
        try {
            return configuration.instance.maxClubsPerUser;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getMaxMembersPerClub() {
        lock.readLock().lock();
        try {
            return configuration.instance.maxMembersPerClub;
        } finally {
            lock.readLock().unlock();
        }
    }

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
     * Updates instance-wide settings. Admin only.
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
     * Gets club-specific flags. Returns default values if club not configured.
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
                        data.allowMemberInvites
                );
            }
            // Return defaults for unconfigured clubs
            return ClubFlags.defaults();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Creates or updates club-specific flags.
     */
    public void setClubFlags(Long clubId, ClubFlags flags) throws IOException {
        lock.writeLock().lock();
        try {
            String key = clubId.toString();
            ClubFlagsData data = new ClubFlagsData();
            data.allowPublicNotes = flags.allowPublicNotes();
            data.requireMeetingRSVP = flags.requireMeetingRSVP();
            data.allowMemberInvites = flags.allowMemberInvites();

            configuration.clubs.put(key, data);
            persistConfiguration();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Creates default flags for a new club.
     */
    public void initializeClubFlags(Long clubId) throws IOException {
        setClubFlags(clubId, ClubFlags.defaults());
    }

    /**
     * Removes club configuration when a club is deleted.
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

    private void loadConfiguration() throws IOException {
        if (!Files.exists(configFilePath)) {
            System.out.println("Config file not found. Creating with defaults...");
            createDefaultConfiguration();
            return;
        }

        try {
            String json = Files.readString(configFilePath);
            configuration = objectMapper.readValue(json, InstanceConfiguration.class);
        } catch (IOException e) {
            System.err.println("Failed to parse config file. Creating backup and using defaults.");
            backupCorruptedFile();
            createDefaultConfiguration();
        }
    }

    private void persistConfiguration() throws IOException {
        String json = objectMapper.writeValueAsString(configuration);
        Files.writeString(configFilePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void createDefaultConfiguration() throws IOException {
        configuration = new InstanceConfiguration();
        configuration.instance = new InstanceData();
        configuration.instance.registrationMode = "INVITE_ONLY";
        configuration.instance.clubCreationMode = "APPROVAL_REQUIRED";
        configuration.instance.maxClubsPerUser = 5;
        configuration.instance.maxMembersPerClub = 50;
        configuration.clubs = new HashMap<>();

        persistConfiguration();
        setRestrictivePermissions();
    }

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

    private void backupCorruptedFile() throws IOException {
        Path backup = configFilePath.resolveSibling(CONFIG_FILENAME + ".corrupted." + System.currentTimeMillis());
        Files.move(configFilePath, backup, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Corrupted config backed up to: " + backup);
    }

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

    public Path getConfigFilePath() {
        return configFilePath;
    }

    // ====== DATA CLASSES ======

    private static class InstanceConfiguration {
        public InstanceData instance;
        public Map<String, ClubFlagsData> clubs;
    }

    private static class InstanceData {
        public String registrationMode;
        public String clubCreationMode;
        public int maxClubsPerUser;
        public int maxMembersPerClub;
    }

    private static class ClubFlagsData {
        public boolean allowPublicNotes;
        public boolean requireMeetingRSVP;
        public boolean allowMemberInvites;
    }

    // ==================== PUBLIC RECORDS ====================

    public record InstanceSettings(
            String registrationMode,
            String clubCreationMode,
            int maxClubsPerUser,
            int maxMembersPerClub
    ) {}

    public record ClubFlags(
            boolean allowPublicNotes,
            boolean requireMeetingRSVP,
            boolean allowMemberInvites
    ) {
        public static ClubFlags defaults() {
            return new ClubFlags(true, false, true);
        }
    }
}