package com.litclub.ui.main.shared.view.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.Reply;
import com.litclub.persistence.repository.ClubRepository;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

/**
 * Service layer for managing replies to notes.
 * Handles both discussion prompt replies and independent club note replies.
 */
public class ReplyService {

    private final ClubRepository clubRepository;

    public ReplyService() {
        this.clubRepository = ClubRepository.getInstance();
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Load replies for a note in an independent club context (non-discussion).
     */
    public void loadIndependentReplies(
            Long bookID,
            Long noteID,
            Runnable onSuccess,
            Consumer<String> onError
    ) {
        clubRepository.fetchIndependentClubReplies(bookID, noteID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Independent Replies Loaded for Book: " + bookID + " Note: " + noteID);
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to load Independent Replies: " + errorMessage);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Load replies for a note within a discussion prompt.
     */
    public void loadDiscussionReplies(
            Long clubID,
            Long promptID,
            Long noteID,
            Runnable onSuccess,
            Consumer<String> onError
    ) {
        clubRepository.fetchDiscussionReplies(clubID, promptID, noteID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Discussion Replies loaded for Prompt: " + promptID + " Note: " + noteID);
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to load Discussion Replies: " + errorMessage);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Get the observable list of replies from the repository.
     */
    public ObservableList<Reply> getReplies() {
        return clubRepository.getReplies();
    }

    // ==================== CREATE OPERATIONS ====================

    /**
     * Create a reply on an independent club note.
     */
    public void createIndependentReply(
            Long noteID,
            Long bookID,
            String content,
            Consumer<Reply> onSuccess,
            Consumer<String> onError
    ) {
        clubRepository.createIndependentClubReply(noteID, bookID, content)
                .thenAccept(reply -> {
                    Platform.runLater(() -> {
                        System.out.println("Independent Reply created for Book: " + bookID + " Note: " + noteID);
                        onSuccess.accept(reply);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to create Independent Reply: " + errorMessage);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Create a reply on a discussion prompt note.
     */
    public void createDiscussionReply(
            Long clubID,
            Long promptID,
            Long noteID,
            String content,
            Consumer<Reply> onSuccess,
            Consumer<String> onError
    ) {
        clubRepository.createDiscussionReply(clubID, promptID, noteID, content)
                .thenAccept(reply -> {
                    Platform.runLater(() -> {
                        System.out.println("Discussion Reply created for Prompt: " + promptID + " Note: " + noteID);
                        onSuccess.accept(reply);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to create Discussion Reply: " + errorMessage);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Update a reply on an independent club note.
     */
    public void updateIndependentReply(
            Long noteID,
            Long bookID,
            Long replyID,
            String content,
            Consumer<Reply> onSuccess,
            Consumer<String> onError
    ) {
        clubRepository.updateIndependentClubReply(noteID, bookID, content, replyID)
                .thenAccept(reply -> {
                    Platform.runLater(() -> {
                        System.out.println("Independent Reply updated: " + replyID);
                        onSuccess.accept(reply);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to update Independent Reply: " + errorMessage);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Update a reply on a discussion prompt note.
     */
    public void updateDiscussionReply(
            Long clubID,
            Long promptID,
            Long noteID,
            Long replyID,
            String content,
            Consumer<Reply> onSuccess,
            Consumer<String> onError
    ) {
        clubRepository.updateDiscussionReply(clubID, promptID, noteID, replyID, content)
                .thenAccept(reply -> {
                    Platform.runLater(() -> {
                        System.out.println("Discussion Reply updated: " + replyID);
                        onSuccess.accept(reply);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to update Discussion Reply: " + errorMessage);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete a reply from an independent club note.
     */
    public void deleteIndependentReply(
            Long noteID,
            Long bookID,
            Long replyID,
            Runnable onSuccess,
            Consumer<String> onError
    ) {
        clubRepository.deleteIndependentClubReply(noteID, bookID, replyID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Independent Reply deleted: " + replyID);
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to delete Independent Reply: " + errorMessage);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Delete a reply from a discussion prompt note.
     */
    public void deleteDiscussionReply(
            Long clubID,
            Long promptID,
            Long noteID,
            Long replyID,
            Runnable onSuccess,
            Consumer<String> onError
    ) {
        clubRepository.deleteDiscussionReply(clubID, promptID, noteID, replyID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Discussion Reply deleted: " + replyID);
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to delete Discussion Reply: " + errorMessage);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }
}