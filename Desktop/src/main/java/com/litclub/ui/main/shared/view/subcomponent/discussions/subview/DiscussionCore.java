package com.litclub.ui.main.shared.view.subcomponent.discussions.subview;

import com.litclub.construct.DiscussionPrompt;
import com.litclub.construct.Note;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.event.EventBus.EventType;
import com.litclub.ui.main.shared.view.service.DiscussionService;
import com.litclub.ui.main.shared.view.subcomponent.discussions.PromptFocus;
import com.litclub.ui.main.shared.view.subcomponent.notes.subview.NoteFocus;
import javafx.scene.layout.StackPane;

/**
 * Core container for discussion view that manages navigation between:
 * - DefaultDiscussionCore: List view of all prompts
 * - PromptFocus: Detailed view of a single prompt with its notes
 * - NoteFocus: Detailed view of a single note from a prompt
 */
public class DiscussionCore extends StackPane {

    private final DefaultDiscussionCore defaultDiscussionCore;
    private final PromptFocus promptFocus;
    private NoteFocus noteFocus; // Remove final

    public DiscussionCore() {
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().add("discussion-core");

        // Create subviews
        defaultDiscussionCore = new DefaultDiscussionCore(this::navigateToPrompt);
        promptFocus = new PromptFocus(
                defaultDiscussionCore.getDiscussionService(),
                this::navigateBackFromPrompt,
                this::navigateToNoteFromPrompt
        );
        noteFocus = new NoteFocus(false, this::navigateBackFromNote);

        // Add all to stack (only one visible at a time)
        this.getChildren().addAll(defaultDiscussionCore, promptFocus, noteFocus);

        EventBus.getInstance().on(EventType.PROMPT_NOTE_UPDATED, () -> {
            if (promptFocus.isVisible()) {
                promptFocus.loadPrompt(promptFocus.getCurrentPrompt());
            }
        });

        // Show default view initially
        showDefaultView();
    }

    // ==================== NAVIGATION ====================

    /**
     * Navigate to prompt focus showing all notes for that prompt.
     */
    private void navigateToPrompt(DiscussionPrompt prompt) {
        System.out.println("Navigating to prompt: " + prompt.getPromptID());

        // Load the prompt into focus view
        promptFocus.loadPrompt(prompt);

        // Show prompt focus, hide others
        defaultDiscussionCore.setVisible(false);
        promptFocus.setVisible(true);
        noteFocus.setVisible(false);
    }

    /**
     * Navigate from prompt focus to a specific note.
     */
    private void navigateToNoteFromPrompt(Note note) {
        System.out.println("Navigating to note from prompt: " + note.getNoteID());

        // Get the current prompt
        DiscussionPrompt currentPrompt = promptFocus.getCurrentPrompt();

        // Remove old noteFocus and create new one with prompt
        this.getChildren().remove(noteFocus);
        noteFocus = new NoteFocus(this::navigateBackFromNote, currentPrompt);
        this.getChildren().add(noteFocus);

        // Load the note into focus view
        noteFocus.loadNote(note);

        // Show note focus, hide others
        promptFocus.setVisible(false);
        noteFocus.setVisible(true);
    }

    /**
     * Navigate back from note focus to prompt focus.
     */
    private void navigateBackFromNote() {
        System.out.println("Navigating back to prompt from note");

        // Show prompt focus, hide note focus
        noteFocus.setVisible(false);
        promptFocus.setVisible(true);

        // Refresh prompt in case notes changed
        // (prompt focus has its own listener, so this might be redundant)
    }

    /**
     * Navigate back from prompt focus to default view.
     */
    private void navigateBackFromPrompt() {
        System.out.println("Navigating back to prompts list");

        // Show default view, hide others
        promptFocus.setVisible(false);
        noteFocus.setVisible(false);
        defaultDiscussionCore.setVisible(true);

        // Refresh default view in case prompts changed
        defaultDiscussionCore.refresh();
    }

    /**
     * Show default prompts list view.
     */
    private void showDefaultView() {
        defaultDiscussionCore.setVisible(true);
        promptFocus.setVisible(false);
        noteFocus.setVisible(false);
    }

    // ==================== PUBLIC API ====================

    /**
     * Refresh the discussion view.
     */
    public void refresh() {
        defaultDiscussionCore.refresh();
    }

    /**
     * Get the discussion service instance.
     */
    public DiscussionService getDiscussionService() {
        return defaultDiscussionCore.getDiscussionService();
    }

    /**
     * Check if currently viewing a focused prompt or note.
     */
    public boolean isViewingFocus() {
        return promptFocus.isVisible() || noteFocus.isVisible();
    }

    /**
     * Navigate back to default view (public method for external navigation).
     */
    public void showPromptsGrid() {
        navigateBackFromPrompt();
    }

    /**
     * Reload prompts from server.
     */
    public void refreshPrompts() {
        defaultDiscussionCore.loadPrompts();
    }
}