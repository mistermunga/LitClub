package com.litclub.persistence.repository;

import com.litclub.client.api.ApiClient;
import com.litclub.construct.interfaces.config.ConfigurationManager;
import com.litclub.construct.interfaces.config.LoadedInstanceSettings;
import com.litclub.construct.interfaces.user.UserRecord;
import com.litclub.session.AppSession;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.concurrent.CompletableFuture;

/**
 * Repository managing instance-level configuration and admin operations.
 *
 * <p>This lightweight repository handles instance settings and administrative
 * functions. Unlike other repositories, it uses JavaFX properties for reactive
 * configuration updates.</p>
 *
 * <p><strong>Thread Safety:</strong> All property modifications happen on JavaFX
 * Application Thread via Platform.runLater().</p>
 */
public class InstanceRepository {

    private static InstanceRepository instance;

    private final ApiClient apiClient;

    // Observable property for instance settings
    private final ObjectProperty<ConfigurationManager.InstanceSettings> instanceSettings;

    private InstanceRepository() {
        this.apiClient = ApiClient.getInstance();
        this.instanceSettings = new SimpleObjectProperty<>();
    }

    public static synchronized InstanceRepository getInstance() {
        if (instance == null) {
            instance = new InstanceRepository();
        }
        return instance;
    }

    // ==================== INSTANCE SETTINGS ====================

    /**
     * Fetches the current instance settings.
     * Available to all authenticated users.
     *
     * @return CompletableFuture with instance settings
     */
    public CompletableFuture<LoadedInstanceSettings> fetchInstanceSettings() {
        return apiClient.get("/api/admins/settings", LoadedInstanceSettings.class)
                .thenApply(settings -> {
                    Platform.runLater(() -> {
                        instanceSettings.set(settings.instanceSettings());
                        AppSession.getInstance().setAdmin(settings.isAdmin());
                    });
                    return settings;
                });
    }

    /**
     * Updates instance settings (admin only).
     *
     * @param settings the new settings to apply
     * @return CompletableFuture with updated settings
     */
    public CompletableFuture<ConfigurationManager.InstanceSettings> updateInstanceSettings(ConfigurationManager.InstanceSettings settings) {
        return apiClient.put("/api/admins/settings", settings, ConfigurationManager.InstanceSettings.class)
                .thenApply(updatedSettings -> {
                    Platform.runLater(() -> {
                        instanceSettings.set(updatedSettings);
                    });
                    return updatedSettings;
                });
    }

    /**
     * Gets the observable property for instance settings.
     * UI components can bind to this for reactive updates.
     *
     * @return ObjectProperty containing current instance settings
     */
    public ObjectProperty<ConfigurationManager.InstanceSettings> instanceSettingsProperty() {
        return instanceSettings;
    }

    /**
     * Gets the current instance settings value.
     *
     * @return current instance settings, or null if not loaded
     */
    public ConfigurationManager.InstanceSettings getInstanceSettings() {
        return instanceSettings.get();
    }

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Promotes a user to administrator role (admin only).
     *
     * @param userID the user's ID to promote
     * @return CompletableFuture with updated user record
     */
    public CompletableFuture<UserRecord> promoteToAdmin(Long userID) {
        return apiClient.post("/api/admins/elevate", userID, UserRecord.class);
    }

    // ==================== UTILITY ====================

    /**
     * Clears cached instance settings.
     */
    public void clearSettings() {
        Platform.runLater(() -> {
            instanceSettings.set(null);
        });
    }
}

