package com.litclub.ui.main.shared.view;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.event.EventBus.EventType;
import com.litclub.ui.main.shared.view.subcomponent.discussions.subview.DiscussionCore;
import javafx.scene.layout.BorderPane;

/**
 * Main discussion view that manages discussion prompts and their notes.
 * Follows the same pattern as NotesView with layered navigation.
 */
public class DiscussionView extends BorderPane {

    private final DiscussionCore discussionCore;

    public DiscussionView() {
        ThemeManager.getInstance().registerComponent(this);

        // Create core
        discussionCore = new DiscussionCore();

        this.setCenter(discussionCore);

        // Subscribe to discussion events
        EventBus.getInstance().on(EventType.DISCUSSION_PROMPTS_UPDATED, discussionCore::refreshPrompts);
    }

    /**
     * Refresh the discussion view.
     */
    public void refresh() {
        discussionCore.refresh();
    }

    /**
     * Navigate back to prompts grid if currently viewing a focused prompt.
     */
    public void showPromptsGrid() {
        if (discussionCore.isViewingFocus()) {
            discussionCore.showPromptsGrid();
        }
    }

    /**
     * Check if currently viewing a focused prompt or note.
     */
    public boolean isViewingFocus() {
        return discussionCore.isViewingFocus();
    }
}