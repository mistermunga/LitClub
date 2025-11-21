package com.litclub.ui.main.shared.view.subcomponent.common;

import com.litclub.theme.ThemeManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Abstract base class for async dialogs with common loading/error handling patterns.
 *
 * <p>Provides standardized:
 * <ul>
 *   <li>Loading indicators and status messages</li>
 *   <li>Button state management during async operations</li>
 *   <li>Success/error feedback with auto-close on success</li>
 *   <li>Form validation hooks</li>
 * </ul>
 *
 * <p><strong>Subclass Template:</strong>
 * <pre>
 * public class MyDialog extends BaseAsyncDialog&lt;MyResultType&gt; {
 *     public MyDialog() {
 *         super("Dialog Title", "Submit");
 *         initializeUI(); // IMPORTANT: Call this after constructor setup
 *     }
 *
 *     &#64;Override
 *     protected Node createFormContent() {
 *         // Build your form UI here
 *     }
 *
 *     &#64;Override
 *     protected boolean validateForm() {
 *         // Validate inputs, return false to prevent submission
 *     }
 *
 *     &#64;Override
 *     protected void handleAsyncSubmit() {
 *         // Make async call here, then call:
 *         // - onSubmitSuccess(result) on success
 *         // - onSubmitError(errorMessage) on error
 *     }
 * }
 * </pre>
 *
 * @param <T> the type of result this dialog produces
 */
public abstract class BaseAsyncDialog<T> extends Dialog<T> {

    // UI Components (protected so subclasses can customize)
    protected Button submitButton;
    protected Button cancelButton;
    protected Label statusLabel;
    protected ProgressIndicator loadingIndicator;
    protected VBox mainContainer;

    // State management
    protected boolean isSubmitting = false;
    protected T dialogResult = null;

    // Configuration
    private final String submitButtonText;
    private final double defaultSuccessDelay;

    // UI initialization flag
    private boolean uiInitialized = false;

    /**
     * Creates a new async dialog with default settings.
     * IMPORTANT: Subclasses must call initializeUI() after their constructor completes.
     *
     * @param title dialog title
     * @param submitButtonText text for the submit button (e.g., "Add", "Create", "Submit")
     */
    protected BaseAsyncDialog(String title, String submitButtonText) {
        this(title, submitButtonText, 1.5);
    }

    /**
     * Creates a new async dialog with custom success delay.
     * IMPORTANT: Subclasses must call initializeUI() after their constructor completes.
     *
     * @param title dialog title
     * @param submitButtonText text for the submit button
     * @param successDelaySeconds seconds to wait before auto-closing on success
     */
    protected BaseAsyncDialog(String title, String submitButtonText, double successDelaySeconds) {
        this.submitButtonText = submitButtonText;
        this.defaultSuccessDelay = successDelaySeconds;

        setTitle(title);
        setResizable(true);

        // Note: UI creation is deferred until initializeUI() is called
    }

    /**
     * Initializes the dialog UI.
     * MUST be called by subclasses after their constructor completes.
     * This ensures all subclass fields are initialized before UI creation.
     */
    protected final void initializeUI() {
        if (uiInitialized) {
            throw new IllegalStateException("UI already initialized");
        }

        // Setup dialog buttons
        ButtonType submitButtonType = new ButtonType(submitButtonText, ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // Build main UI
        mainContainer = createMainContainer();
        getDialogPane().setContent(mainContainer);

        // Get button references
        submitButton = (Button) getDialogPane().lookupButton(submitButtonType);
        cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);

        // Initial button state
        submitButton.setDisable(!isFormValid());

        // Setup form validation listener
        setupFormValidation();

        // Prevent dialog from closing on submit button
        submitButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!isSubmitting) {
                event.consume();
                handleSubmit();
            }
        });

        // Result converter (returns null by default, result set via onSubmitSuccess)
        setResultConverter(buttonType -> dialogResult);

        ThemeManager.getInstance().registerComponent(this.getDialogPane());
        getDialogPane().getStyleClass().add("root");

        submitButton.getStyleClass().add("button-primary");
        cancelButton.getStyleClass().add("secondary-button");

        uiInitialized = true;
    }

    // ==================== ABSTRACT METHODS (must be implemented by subclasses) ====================

    /**
     * Creates the main form content (fields, labels, etc.).
     * This is called once during dialog construction.
     *
     * @return the form content node
     */
    protected abstract Node createFormContent();

    /**
     * Validates the form before submission.
     * Called when the submit button is clicked.
     *
     * @return true if form is valid and submission should proceed
     */
    protected abstract boolean validateForm();

    /**
     * Performs the async operation (API call, etc.).
     * Must call either onSubmitSuccess() or onSubmitError() when complete.
     */
    protected abstract void handleAsyncSubmit();

    // ==================== OPTIONAL HOOKS (can be overridden for customization) ====================

    /**
     * Hook called when form validation state changes.
     * Override to enable/disable submit button based on field values.
     *
     * @return true if form is currently valid
     */
    protected boolean isFormValid() {
        return true;
    }

    /**
     * Hook to set up listeners for form validation.
     * Override to add listeners to form fields that call updateSubmitButtonState().
     */
    protected void setupFormValidation() {
        // Default: no validation listeners
    }

    /**
     * Gets the success message to display.
     * Override to customize success message.
     *
     * @param result the result object
     * @return success message
     */
    protected String getSuccessMessage(T result) {
        return "Operation completed successfully!";
    }

    /**
     * Gets the delay (in seconds) before auto-closing on success.
     * Override to customize or return -1 to disable auto-close.
     *
     * @return delay in seconds, or -1 to disable auto-close
     */
    protected double getSuccessCloseDelay() {
        return defaultSuccessDelay;
    }

    // ==================== UI CONSTRUCTION ====================

    private VBox createMainContainer() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setMinWidth(500);
        container.getStyleClass().add("container");

        // Add header if provided
        String header = getHeaderText();
        if (header != null && !header.isEmpty()) {
            Label headerLabel = new Label(header);
            headerLabel.getStyleClass().add("label");
            headerLabel.setStyle("-fx-font-size: 14px; -fx-padding: 0 0 10 0;");
            container.getChildren().add(headerLabel);
        }

        // Add form content
        Node formContent = createFormContent();
        container.getChildren().add(formContent);

        // Add status label
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(450);
        container.getChildren().add(statusLabel);

        // Add loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(30, 30);
        loadingIndicator.setVisible(false);
        container.getChildren().add(loadingIndicator);

        return container;
    }

    // ==================== SUBMISSION FLOW ====================

    private void handleSubmit() {
        if (isSubmitting) return;

        // Validate form
        if (!validateForm()) {
            return; // Validation failed, error should be shown by validateForm()
        }

        // Enter submitting state
        isSubmitting = true;
        setLoadingState(true);
        showLoading();

        // Perform async operation
        handleAsyncSubmit();
    }

    /**
     * Call this from handleAsyncSubmit() when the operation succeeds.
     *
     * @param result the result to return from the dialog
     */
    protected final void onSubmitSuccess(T result) {
        Platform.runLater(() -> {
            this.dialogResult = result;
            setLoadingState(false);
            showSuccess(getSuccessMessage(result));

            double delay = getSuccessCloseDelay();
            if (delay > 0) {
                // Auto-close after delay
                PauseTransition pause = new PauseTransition(Duration.seconds(delay));
                pause.setOnFinished(e -> close());
                pause.play();
            }
        });
    }

    /**
     * Call this from handleAsyncSubmit() when the operation fails.
     *
     * @param errorMessage user-friendly error message
     */
    protected final void onSubmitError(String errorMessage) {
        Platform.runLater(() -> {
            setLoadingState(false);
            resetSubmittingState();
            showError(errorMessage);
        });
    }

    // ==================== UI STATE MANAGEMENT ====================

    private void setLoadingState(boolean loading) {
        submitButton.setDisable(loading);
        cancelButton.setDisable(loading);
        loadingIndicator.setVisible(loading);
    }

    private void resetSubmittingState() {
        isSubmitting = false;
        submitButton.setDisable(!isFormValid());
        cancelButton.setDisable(false);
    }

    /**
     * Updates the submit button enabled state based on current form validity.
     * Call this from field listeners when form values change.
     */
    protected final void updateSubmitButtonState() {
        if (!isSubmitting && uiInitialized) {
            submitButton.setDisable(!isFormValid());
        }
    }

    // ==================== STATUS DISPLAY ====================

    private void showLoading() {
        statusLabel.setText("Processing...");
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        statusLabel.getStyleClass().add("info-label");
        statusLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error-label", "info-label");
        statusLabel.getStyleClass().add("success-label");
        statusLabel.setVisible(true);
    }

    /**
     * Shows an error message to the user.
     * Can be called from validateForm() or anywhere else.
     *
     * @param message error message to display
     */
    protected final void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("success-label", "info-label");
        statusLabel.getStyleClass().add("error-label");
        statusLabel.setVisible(true);
    }

    /**
     * Hides the status label.
     */
    protected final void hideStatus() {
        statusLabel.setVisible(false);
    }
}