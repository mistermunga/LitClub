package com.litclub.ui.main.shared.view.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.Note;
import com.litclub.construct.interfaces.note.NoteCreateRequest;
import com.litclub.persistence.repository.ClubRepository;
import com.litclub.persistence.repository.LibraryRepository;
import com.litclub.session.AppSession;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

/**
 * Service layer for note operations.
 *
 * <p>Handles both personal notes (private, user-scoped) and club notes (public, club-scoped).
 * All notes are associated with a book. Club notes may optionally be associated with a
 * discussion prompt.</p>
 *
 * <p>Follows the service layer pattern established by LibraryService:
 * - Delegates to repositories
 * - Provides UI-friendly callback-based methods
 * - Handles error translation via ApiErrorHandler
 * - Exposes repository ObservableLists directly for reactive UI binding</p>
 */
public class NoteService {

    private final LibraryRepository libraryRepository;
    private final ClubRepository clubRepository;
    private final AppSession session;

    public NoteService() {
        this.libraryRepository = LibraryRepository.getInstance();
        this.clubRepository = ClubRepository.getInstance();
        this.session = AppSession.getInstance();
    }

    // ==================== DATA ACCESS (delegates to repositories) ====================

    /**
     * Gets the observable list of personal notes.
     * Personal notes are private and belong to the current user.
     *
     * @return observable list of personal notes
     */
    public ObservableList<Note> getPersonalNotes() {
        return libraryRepository.getPersonalNotes();
    }

    /**
     * Gets the observable list of club notes.
     * Club notes are public within the club and may be attached to discussion prompts.
     *
     * @return observable list of club notes
     */
    public ObservableList<Note> getClubNotes() {
        return clubRepository.getClubNotes();
    }

    // ==================== PERSONAL NOTE OPERATIONS ====================

    /**
     * Load user's personal notes from API.
     *
     * @param userID the user's ID
     * @param onSuccess callback when notes load successfully
     * @param onError callback with user-friendly error message
     */
    public void loadPersonalNotes(Long userID,
                                  Runnable onSuccess,
                                  Consumer<String> onError) {

        libraryRepository.fetchPersonalNotes(userID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Personal notes loaded successfully");
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to load personal notes: " + errorMessage);
                        onError.accept("Failed to load personal notes: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Create a new personal note.
     * Personal notes are always private and must be associated with a book.
     *
     * @param userID the user's ID
     * @param request note content and metadata (must include bookID)
     * @param onSuccess callback with the created note
     * @param onError callback with user-friendly error message
     */
    public void createPersonalNote(Long userID,
                                   NoteCreateRequest request,
                                   Consumer<Note> onSuccess,
                                   Consumer<String> onError) {

        // Validate that bookID is present
        if (request.bookID() == null) {
            Platform.runLater(() -> onError.accept("Book ID is required for personal notes"));
            return;
        }

        libraryRepository.createPersonalNote(userID, request)
                .thenAccept(note -> {
                    Platform.runLater(() -> {
                        System.out.println("Personal note created: " + note.getNoteID());
                        onSuccess.accept(note);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to create personal note: " + errorMessage);
                        onError.accept("Failed to create personal note: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Update an existing personal note's content.
     *
     * @param userID the user's ID
     * @param noteID the note's ID
     * @param content new note content
     * @param onSuccess callback with the updated note
     * @param onError callback with user-friendly error message
     */
    public void updatePersonalNote(Long userID,
                                   Long noteID,
                                   String content,
                                   Consumer<Note> onSuccess,
                                   Consumer<String> onError) {

        libraryRepository.updatePersonalNote(userID, noteID, content)
                .thenAccept(note -> {
                    Platform.runLater(() -> {
                        System.out.println("Personal note updated: " + noteID);
                        onSuccess.accept(note);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to update personal note: " + errorMessage);
                        onError.accept("Failed to update personal note: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Delete a personal note.
     *
     * @param userID the user's ID
     * @param noteID the note's ID
     * @param onSuccess callback when note is deleted
     * @param onError callback with user-friendly error message
     */
    public void deletePersonalNote(Long userID,
                                   Long noteID,
                                   Runnable onSuccess,
                                   Consumer<String> onError) {

        libraryRepository.deletePersonalNote(userID, noteID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Personal note deleted: " + noteID);
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to delete personal note: " + errorMessage);
                        onError.accept("Failed to delete personal note: " + errorMessage);
                    });
                    return null;
                });
    }

    // ==================== CLUB NOTE OPERATIONS ====================

    /**
     * Load club notes (not attached to a specific discussion prompt).
     *
     * @param clubID the club's ID
     * @param onSuccess callback when notes load successfully
     * @param onError callback with user-friendly error message
     */
    public void loadClubNotes(Long clubID,
                              Runnable onSuccess,
                              Consumer<String> onError) {

        clubRepository.fetchClubNotes(clubID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Club notes loaded successfully");
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to load club notes: " + errorMessage);
                        onError.accept("Failed to load club notes: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Load notes for a specific discussion prompt.
     *
     * @param clubID the club's ID
     * @param promptID the discussion prompt's ID
     * @param onSuccess callback when notes load successfully
     * @param onError callback with user-friendly error message
     */
    public void loadPromptNotes(Long clubID,
                                Long promptID,
                                Runnable onSuccess,
                                Consumer<String> onError) {

        clubRepository.fetchPromptNotes(clubID, promptID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Prompt notes loaded successfully");
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to load prompt notes: " + errorMessage);
                        onError.accept("Failed to load prompt notes: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Create a club note (not attached to a discussion prompt).
     * Club notes are public within the club and must be associated with a book.
     *
     * @param clubID the club's ID
     * @param request note content and metadata (must include bookID)
     * @param onSuccess callback with the created note
     * @param onError callback with user-friendly error message
     */
    public void createClubNote(Long clubID,
                               NoteCreateRequest request,
                               Consumer<Note> onSuccess,
                               Consumer<String> onError) {

        // Validate that bookID is present
        if (request.bookID() == null) {
            Platform.runLater(() -> onError.accept("Book ID is required for club notes"));
            return;
        }

        clubRepository.createClubNote(clubID, request)
                .thenAccept(note -> {
                    Platform.runLater(() -> {
                        System.out.println("Club note created: " + note.getNoteID());
                        onSuccess.accept(note);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to create club note: " + errorMessage);
                        onError.accept("Failed to create club note: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Create a note attached to a discussion prompt.
     * These notes are public within the club and associated with both a book and a prompt.
     *
     * @param clubID the club's ID
     * @param promptID the discussion prompt's ID
     * @param request note content and metadata (must include bookID)
     * @param onSuccess callback with the created note
     * @param onError callback with user-friendly error message
     */
    public void createPromptNote(Long clubID,
                                 Long promptID,
                                 NoteCreateRequest request,
                                 Consumer<Note> onSuccess,
                                 Consumer<String> onError) {

        // Validate that bookID is present
        if (request.bookID() == null) {
            Platform.runLater(() -> onError.accept("Book ID is required for discussion notes"));
            return;
        }

        clubRepository.createPromptNote(clubID, promptID, request)
                .thenAccept(note -> {
                    Platform.runLater(() -> {
                        System.out.println("Prompt note created: " + note.getNoteID());
                        onSuccess.accept(note);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to create prompt note: " + errorMessage);
                        onError.accept("Failed to create prompt note: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Update a club note's content.
     *
     * @param clubID the club's ID
     * @param noteID the note's ID
     * @param request updated note content
     * @param onSuccess callback with the updated note
     * @param onError callback with user-friendly error message
     */
    public void updateClubNote(Long clubID,
                               Long noteID,
                               NoteCreateRequest request,
                               Consumer<Note> onSuccess,
                               Consumer<String> onError) {

        clubRepository.updateClubNote(clubID, noteID, request)
                .thenAccept(note -> {
                    Platform.runLater(() -> {
                        System.out.println("Club note updated: " + noteID);
                        onSuccess.accept(note);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to update club note: " + errorMessage);
                        onError.accept("Failed to update club note: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Update a prompt note's content.
     *
     * @param clubID the club's ID
     * @param promptID the discussion prompt's ID
     * @param noteID the note's ID
     * @param request updated note content
     * @param onSuccess callback with the updated note
     * @param onError callback with user-friendly error message
     */
    public void updatePromptNote(Long clubID,
                                 Long promptID,
                                 Long noteID,
                                 NoteCreateRequest request,
                                 Consumer<Note> onSuccess,
                                 Consumer<String> onError) {

        clubRepository.updatePromptNote(clubID, promptID, noteID, request)
                .thenAccept(note -> {
                    Platform.runLater(() -> {
                        System.out.println("Prompt note updated: " + noteID);
                        onSuccess.accept(note);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to update prompt note: " + errorMessage);
                        onError.accept("Failed to update prompt note: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Delete a club note.
     *
     * @param clubID the club's ID
     * @param noteID the note's ID
     * @param onSuccess callback when note is deleted
     * @param onError callback with user-friendly error message
     */
    public void deleteClubNote(Long clubID,
                               Long noteID,
                               Runnable onSuccess,
                               Consumer<String> onError) {

        clubRepository.deleteClubNote(clubID, noteID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Club note deleted: " + noteID);
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to delete club note: " + errorMessage);
                        onError.accept("Failed to delete club note: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Delete a prompt note.
     *
     * @param clubID the club's ID
     * @param promptID the discussion prompt's ID
     * @param noteID the note's ID
     * @param onSuccess callback when note is deleted
     * @param onError callback with user-friendly error message
     */
    public void deletePromptNote(Long clubID,
                                 Long promptID,
                                 Long noteID,
                                 Runnable onSuccess,
                                 Consumer<String> onError) {

        clubRepository.deletePromptNote(clubID, promptID, noteID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Prompt note deleted: " + noteID);
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to delete prompt note: " + errorMessage);
                        onError.accept("Failed to delete prompt note: " + errorMessage);
                    });
                    return null;
                });
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get current user's ID from session.
     *
     * @return user ID or null if not logged in
     */
    public Long getCurrentUserId() {
        return session.getUserRecord() != null ? session.getUserRecord().userID() : null;
    }

    /**
     * Get current club's ID from session.
     *
     * @return club ID or null if not in a club context
     */
    public Long getCurrentClubId() {
        return session.getCurrentClub() != null ? session.getCurrentClub().getClubID() : null;
    }

    /**
     * Check if a note belongs to the current user.
     *
     * @param note the note to check
     * @return true if note belongs to current user
     */
    public boolean isNoteOwnedByCurrentUser(Note note) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null &&
                note.getUser() != null &&
                note.getUser().getUserID().equals(currentUserId);
    }

    /**
     * Check if currently in a personal context (not in a club).
     *
     * @return true if in personal context
     */
    public boolean isPersonalContext() {
        return getCurrentClubId() == null;
    }

    /**
     * Check if currently in a club context.
     *
     * @return true if in club context
     */
    public boolean isClubContext() {
        return getCurrentClubId() != null;
    }

    /**
     * Get total count of personal notes.
     *
     * @return number of personal notes
     */
    public int getPersonalNotesCount() {
        return getPersonalNotes().size();
    }

    /**
     * Get total count of club notes.
     *
     * @return number of club notes
     */
    public int getClubNotesCount() {
        return getClubNotes().size();
    }

    /**
     * Refresh personal notes from server.
     *
     * @param userID the user's ID
     * @param onSuccess callback when refresh completes
     * @param onError callback with user-friendly error message
     */
    public void refreshPersonalNotes(Long userID,
                                     Runnable onSuccess,
                                     Consumer<String> onError) {
        // Same as loadPersonalNotes - just a semantic alias
        loadPersonalNotes(userID, onSuccess, onError);
    }

    /**
     * Refresh club notes from server.
     *
     * @param clubID the club's ID
     * @param onSuccess callback when refresh completes
     * @param onError callback with user-friendly error message
     */
    public void refreshClubNotes(Long clubID,
                                 Runnable onSuccess,
                                 Consumer<String> onError) {
        // Same as loadClubNotes - just a semantic alias
        loadClubNotes(clubID, onSuccess, onError);
    }
}
