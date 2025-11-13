package com.litclub.persistence.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.litclub.client.api.ApiClient;
import com.litclub.construct.*;
import com.litclub.construct.interfaces.PageResponse;
import com.litclub.construct.interfaces.club.ClubCreateRequest;
import com.litclub.construct.interfaces.discussion.DiscussionThread;
import com.litclub.construct.interfaces.meeting.MeetingCreateRequest;
import com.litclub.construct.interfaces.meeting.MeetingUpdateRequest;
import com.litclub.construct.interfaces.note.NoteCreateRequest;
import com.litclub.construct.interfaces.user.UserRecord;
import com.litclub.persistence.cache.CacheManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repository managing club-related data: clubs, meetings, discussions, and club notes.
 *
 * <p>This repository owns all ObservableLists for club domain objects and coordinates
 * between the API client and local cache. All operations return CompletableFuture for
 * async handling with UI loading states.</p>
 *
 * <p><strong>Thread Safety:</strong> All ObservableList modifications happen on JavaFX
 * Application Thread via Platform.runLater().</p>
 */
public class ClubRepository {

    private static ClubRepository instance;

    private final ApiClient apiClient;
    private final CacheManager cacheManager;

    // Observable data stores
    private final ObservableList<Club> userClubs;
    private final ObservableList<Meeting> meetings;
    private final ObservableList<DiscussionPrompt> discussions;
    private final ObservableList<Note> clubNotes;
    private final ObservableList<Reply> replies;

    private final ObservableList<Meeting> userMeetings;

    private ClubRepository() {
        this.apiClient = ApiClient.getInstance();
        this.cacheManager = CacheManager.getInstance();

        // Initialize observable lists
        this.userClubs = FXCollections.observableArrayList();
        this.meetings = FXCollections.observableArrayList();
        this.userMeetings = FXCollections.observableArrayList();
        this.discussions = FXCollections.observableArrayList();
        this.clubNotes = FXCollections.observableArrayList();
        this.replies = FXCollections.observableArrayList();
    }

    public static synchronized ClubRepository getInstance() {
        if (instance == null) {
            instance = new ClubRepository();
        }
        return instance;
    }

    // ==================== CLUBS ====================

    /**
     * Fetches all clubs for a user using the user endpoint.
     *
     * @param userID the user's ID
     * @return CompletableFuture that completes when clubs are loaded
     */
    public CompletableFuture<Void> fetchUserClubs(Long userID) {
        return apiClient.get("/api/users/" + userID + "/clubs?page=0&size=100",
                        new TypeReference<PageResponse<Club>>() {})
                .thenAccept(pageResponse -> {
                    Platform.runLater(() -> {
                        userClubs.clear();
                        userClubs.addAll(pageResponse.getContent());
                        System.out.println("Loaded " + pageResponse.getContent().size() + " clubs for user " + userID);
                    });
                });
    }

    /**
     * Fetches a specific club's details.
     *
     * @param clubID the club's ID
     * @return CompletableFuture with the club
     */
    public CompletableFuture<Club> fetchClub(Long clubID) {
        return apiClient.get("/api/clubs/" + clubID, Club.class)
                .thenApply(club -> {
                    Platform.runLater(() -> {
                        // Update club in list if it exists
                        userClubs.removeIf(c -> c.getClubID().equals(clubID));
                        userClubs.add(club);
                    });
                    return club;
                });
    }

    /**
     * Creates a new club.
     *
     * @param clubRequest club details
     * @return CompletableFuture with the created club
     */
    public CompletableFuture<Club> createClub(ClubCreateRequest clubRequest) {
        return apiClient.post("/api/clubs", clubRequest, Club.class)
                .thenApply(club -> {
                    Platform.runLater(() -> {
                        userClubs.add(club);
                    });
                    return club;
                });
    }

    /**
     * Updates an existing club.
     *
     * @param clubID the club's ID
     * @param clubRequest updated club details
     * @return CompletableFuture with the updated club
     */
    public CompletableFuture<Club> updateClub(Long clubID, ClubCreateRequest clubRequest) {
        return apiClient.put("/api/clubs/" + clubID, clubRequest, Club.class)
                .thenApply(club -> {
                    Platform.runLater(() -> {
                        // Replace old club with updated one
                        userClubs.removeIf(c -> c.getClubID().equals(clubID));
                        userClubs.add(club);
                    });
                    return club;
                });
    }

    /**
     * Deletes a club.
     *
     * @param clubID the club's ID
     * @return CompletableFuture that completes when club is deleted
     */
    public CompletableFuture<Void> deleteClub(Long clubID) {
        return apiClient.delete("/api/clubs/" + clubID)
                .thenAccept(v -> {
                    Platform.runLater(() -> {
                        userClubs.removeIf(c -> c.getClubID().equals(clubID));
                    });
                });
    }

    /**
     * Joins a club.
     *
     * @param clubID the club's ID
     * @return CompletableFuture with the membership
     */
    public CompletableFuture<ClubMembership> joinClub(Long clubID) {
        return apiClient.post("/api/clubs/" + clubID + "/join", ClubMembership.class)
                .thenApply(membership -> {
                    // Fetch the club details to add to userClubs
                    fetchClub(clubID);
                    return membership;
                });
    }

    /**
     * Leaves a club.
     *
     * @param clubID the club's ID
     * @return CompletableFuture that completes when user has left
     */
    public CompletableFuture<Void> leaveClub(Long clubID) {
        return apiClient.post("/api/clubs/" + clubID + "/leave", Void.class)
                .thenAccept(v -> {
                    Platform.runLater(() -> {
                        userClubs.removeIf(c -> c.getClubID().equals(clubID));
                    });
                });
    }

    /**
     * Fetches members of a club.
     *
     * @param clubID the club's ID
     * @return CompletableFuture with list of members
     */
    public CompletableFuture<List<UserRecord>> fetchClubMembers(Long clubID) {
        return apiClient.get("/api/clubs/" + clubID + "/members?page=0&size=100",
                        new TypeReference<PageResponse<UserRecord>>() {})
                .thenApply(PageResponse::getContent);
    }

    /**
     * Adds a member to a club (moderator only).
     *
     * @param clubID the club's ID
     * @param userRecord the user to add
     * @return CompletableFuture with the membership
     */
    public CompletableFuture<ClubMembership> addMemberToClub(Long clubID, UserRecord userRecord) {
        return apiClient.post("/api/clubs/" + clubID + "/members", userRecord, ClubMembership.class);
    }

    /**
     * Removes a member from a club (moderator only).
     *
     * @param clubID the club's ID
     * @param userID the user's ID to remove
     * @return CompletableFuture that completes when member is removed
     */
    public CompletableFuture<Void> removeMemberFromClub(Long clubID, Long userID) {
        return apiClient.delete("/api/clubs/" + clubID + "/members/" + userID);
    }

    // ==================== MEETINGS ====================

    /**
     * Fetches meetings for a club (fetching multiple pages to get as much as possible).
     *
     * @param clubID the club's ID
     * @return CompletableFuture that completes when meetings are loaded
     */
    public CompletableFuture<Void> fetchClubMeetings(Long clubID) {
        return fetchClubMeetingsRecursive(clubID, 0, new ArrayList<>())
                .thenAccept(allMeetings -> {
                    Platform.runLater(() -> {
                        meetings.clear();
                        meetings.addAll(allMeetings);
                    });

                    cacheManager.saveMeetings(allMeetings);
                });
    }

    /**
     * Fetches meetings for a user across all their clubs.
     *
     * @param userID the user's ID
     * @return CompletableFuture that completes when meetings are loaded
     */
    public CompletableFuture<Void> fetchUserMeetings(Long userID) {
        return apiClient.get("/api/meetings/user/" + userID + "?page=0&size=100",
                        new TypeReference<PageResponse<Meeting>>() {})
                .thenAccept(pageResponse -> {
                    Platform.runLater(() -> {
                        userMeetings.clear();
                        userMeetings.addAll(pageResponse.getContent());
                    });

                    cacheManager.saveMeetings(new ArrayList<>(userMeetings));
                });
    }

    /**
     * Recursively fetches all pages of meetings.
     */
    private CompletableFuture<List<Meeting>> fetchClubMeetingsRecursive(Long clubID, int page, List<Meeting> accumulator) {
        return apiClient.get("/api/clubs/" + clubID + "/meetings?page=" + page + "&size=100",
                        new TypeReference<PageResponse<Meeting>>() {})
                .thenCompose(pageResponse -> {
                    accumulator.addAll(pageResponse.getContent());

                    // If there are more pages, fetch the next one
                    if (pageResponse.hasNext()) {
                        return fetchClubMeetingsRecursive(clubID, page + 1, accumulator);
                    } else {
                        return CompletableFuture.completedFuture(accumulator);
                    }
                });
    }

    /**
     * Fetches a specific meeting.
     *
     * @param clubID the club's ID
     * @param meetingID the meeting's ID
     * @return CompletableFuture with the meeting
     */
    public CompletableFuture<Meeting> fetchMeeting(Long clubID, Long meetingID) {
        return apiClient.get("/api/clubs/" + clubID + "/meetings/" + meetingID, Meeting.class)
                .thenApply(meeting -> {
                    Platform.runLater(() -> {
                        // Update meeting in list if it exists
                        meetings.removeIf(m -> m.getMeetingID().equals(meetingID));
                        meetings.add(meeting);
                    });

                    cacheManager.saveMeetings(new ArrayList<>(meetings));
                    return meeting;
                });
    }

    /**
     * Creates a meeting (moderator only).
     *
     * @param clubID the club's ID
     * @param meetingRequest meeting details
     * @return CompletableFuture with the created meeting
     */
    public CompletableFuture<Meeting> createMeeting(Long clubID, MeetingCreateRequest meetingRequest) {
        return apiClient.post("/api/clubs/" + clubID + "/meetings", meetingRequest, Meeting.class)
                .thenApply(meeting -> {
                    Platform.runLater(() -> {
                        meetings.add(meeting);
                    });

                    cacheManager.saveMeetings(new ArrayList<>(meetings));
                    return meeting;
                });
    }

    /**
     * Updates a meeting (moderator only).
     *
     * @param clubID the club's ID
     * @param meetingID the meeting's ID
     * @param meetingRequest updated meeting details
     * @return CompletableFuture with the updated meeting
     */
    public CompletableFuture<Meeting> updateMeeting(Long clubID, Long meetingID, MeetingUpdateRequest meetingRequest) {
        return apiClient.post("/api/clubs/" + clubID + "/meetings/" + meetingID, meetingRequest, Meeting.class)
                .thenApply(meeting -> {
                    Platform.runLater(() -> {
                        // Replace old meeting with updated one
                        meetings.removeIf(m -> m.getMeetingID().equals(meetingID));
                        meetings.add(meeting);
                    });

                    cacheManager.saveMeetings(new ArrayList<>(meetings));
                    return meeting;
                });
    }

    /**
     * Deletes a meeting (moderator only).
     *
     * @param clubID the club's ID
     * @param meetingID the meeting's ID
     * @return CompletableFuture that completes when meeting is deleted
     */
    public CompletableFuture<Void> deleteMeeting(Long clubID, Long meetingID) {
        return apiClient.delete("/api/clubs/" + clubID + "/meetings/" + meetingID)
                .thenAccept(v -> {
                    Platform.runLater(() -> {
                        meetings.removeIf(m -> m.getMeetingID().equals(meetingID));
                    });

                    cacheManager.saveMeetings(new ArrayList<>(meetings));
                });
    }

    // ==================== DISCUSSIONS ====================

    /**
     * Fetches discussion prompts for a club.
     *
     * @param clubID the club's ID
     * @return CompletableFuture that completes when discussions are loaded
     */
    public CompletableFuture<Void> fetchDiscussions(Long clubID) {
        return apiClient.get("/api/clubs/" + clubID + "/discussions?page=0&size=100",
                        new TypeReference<PageResponse<DiscussionPrompt>>() {})
                .thenAccept(page -> {
                    Platform.runLater(() -> {
                        discussions.clear();
                        discussions.addAll(page.getContent());
                    });

                    cacheManager.savePrompts(new ArrayList<>(discussions));
                });
    }

    /**
     * Creates a discussion prompt (moderator only).
     *
     * @param clubID the club's ID
     * @param prompt the discussion prompt text
     * @return CompletableFuture with the created discussion prompt
     */
    public CompletableFuture<DiscussionPrompt> createDiscussion(Long clubID, String prompt) {
        return apiClient.post("/api/clubs/" + clubID + "/discussions", prompt, DiscussionPrompt.class)
                .thenApply(discussionPrompt -> {
                    Platform.runLater(() -> {
                        discussions.add(discussionPrompt);
                    });

                    cacheManager.savePrompts(new ArrayList<>(discussions));
                    return discussionPrompt;
                });
    }

    /**
     * Fetches a discussion thread (prompt + all notes).
     *
     * @param clubID the club's ID
     * @param promptID the prompt's ID
     * @return CompletableFuture with the discussion thread
     */
    public CompletableFuture<DiscussionThread> fetchDiscussionThread(Long clubID, Long promptID) {
        return apiClient.get("/api/clubs/" + clubID + "/discussions/" + promptID, DiscussionThread.class);
    }

    /**
     * Deletes a discussion prompt (moderator only).
     *
     * @param clubID the club's ID
     * @param promptID the prompt's ID
     * @return CompletableFuture that completes when discussion is deleted
     */
    public CompletableFuture<Void> deleteDiscussion(Long clubID, Long promptID) {
        return apiClient.delete("/api/clubs/" + clubID + "/discussions/" + promptID)
                .thenAccept(v -> {
                    Platform.runLater(() -> {
                        discussions.removeIf(d -> d.getPromptID().equals(promptID));
                    });

                    cacheManager.savePrompts(new ArrayList<>(discussions));
                });
    }

    // ==================== CLUB NOTES ====================

    /**
     * Fetches notes for a club.
     *
     * @param clubID the club's ID
     * @return CompletableFuture that completes when notes are loaded
     */
    public CompletableFuture<Void> fetchClubNotes(Long clubID) {
        return apiClient.get("/api/clubs/" + clubID + "/notes?page=0&size=100",
                        new TypeReference<PageResponse<Note>>() {})
                .thenAccept(page -> {
                    Platform.runLater(() -> {
                        clubNotes.clear();
                        clubNotes.addAll(page.getContent());
                    });

                    cacheManager.saveNotes(new ArrayList<>(clubNotes));
                });
    }

    /**
     * Fetches notes for a specific discussion prompt.
     *
     * @param clubID the club's ID
     * @param promptID the prompt's ID
     * @return CompletableFuture that completes when notes are loaded
     */
    public CompletableFuture<Void> fetchPromptNotes(Long clubID, Long promptID) {
        return apiClient.get("/api/clubs/" + clubID + "/discussions/" + promptID + "/notes?page=0&size=100",
                        new TypeReference<PageResponse<Note>>() {})
                .thenAccept(page -> {
                    Platform.runLater(() -> {
                        clubNotes.clear();
                        clubNotes.addAll(page.getContent());
                    });

                    cacheManager.saveNotes(new ArrayList<>(clubNotes));
                });
    }

    /**
     * Creates a club note.
     *
     * @param clubID the club's ID
     * @param noteRequest note details
     * @return CompletableFuture with the created note
     */
    public CompletableFuture<Note> createClubNote(Long clubID, NoteCreateRequest noteRequest) {
        return apiClient.post("/api/clubs/" + clubID + "/notes", noteRequest, Note.class)
                .thenApply(note -> {
                    Platform.runLater(() -> {
                        clubNotes.add(note);
                    });

                    cacheManager.saveNotes(new ArrayList<>(clubNotes));
                    return note;
                });
    }

    /**
     * Creates a note for a discussion prompt.
     *
     * @param clubID the club's ID
     * @param promptID the prompt's ID
     * @param noteRequest note details
     * @return CompletableFuture with the created note
     */
    public CompletableFuture<Note> createPromptNote(Long clubID, Long promptID, NoteCreateRequest noteRequest) {
        return apiClient.post("/api/clubs/" + clubID + "/discussions/" + promptID + "/notes", noteRequest, Note.class)
                .thenApply(note -> {
                    Platform.runLater(() -> {
                        clubNotes.add(note);
                    });

                    cacheManager.saveNotes(new ArrayList<>(clubNotes));
                    return note;
                });
    }

    /**
     * Updates a club note.
     *
     * @param clubID the club's ID
     * @param noteID the note's ID
     * @param noteRequest updated note content
     * @return CompletableFuture with the updated note
     */
    public CompletableFuture<Note> updateClubNote(Long clubID, Long noteID, NoteCreateRequest noteRequest) {
        return apiClient.put("/api/clubs/" + clubID + "/notes/" + noteID, noteRequest, Note.class)
                .thenApply(note -> {
                    Platform.runLater(() -> {
                        // Replace old note with updated one
                        clubNotes.removeIf(n -> n.getNoteID().equals(noteID));
                        clubNotes.add(note);
                    });

                    cacheManager.saveNotes(new ArrayList<>(clubNotes));
                    return note;
                });
    }

    /**
     * Updates a prompt note.
     *
     * @param clubID the club's ID
     * @param promptID the prompt's ID
     * @param noteID the note's ID
     * @param noteRequest updated note content
     * @return CompletableFuture with the updated note
     */
    public CompletableFuture<Note> updatePromptNote(Long clubID, Long promptID, Long noteID, NoteCreateRequest noteRequest) {
        return apiClient.put("/api/clubs/" + clubID + "/discussions/" + promptID + "/notes/" + noteID,
                        noteRequest, Note.class)
                .thenApply(note -> {
                    Platform.runLater(() -> {
                        // Replace old note with updated one
                        clubNotes.removeIf(n -> n.getNoteID().equals(noteID));
                        clubNotes.add(note);
                    });

                    cacheManager.saveNotes(new ArrayList<>(clubNotes));
                    return note;
                });
    }

    /**
     * Deletes a club note.
     *
     * @param clubID the club's ID
     * @param noteID the note's ID
     * @return CompletableFuture that completes when note is deleted
     */
    public CompletableFuture<Void> deleteClubNote(Long clubID, Long noteID) {
        return apiClient.delete("/api/clubs/" + clubID + "/notes/" + noteID)
                .thenAccept(v -> {
                    Platform.runLater(() -> {
                        clubNotes.removeIf(n -> n.getNoteID().equals(noteID));
                    });

                    cacheManager.saveNotes(new ArrayList<>(clubNotes));
                });
    }

    /**
     * Deletes a prompt note.
     *
     * @param clubID the club's ID
     * @param promptID the prompt's ID
     * @param noteID the note's ID
     * @return CompletableFuture that completes when note is deleted
     */
    public CompletableFuture<Void> deletePromptNote(Long clubID, Long promptID, Long noteID) {
        return apiClient.delete("/api/clubs/" + clubID + "/discussions/" + promptID + "/notes/" + noteID)
                .thenAccept(v -> {
                    Platform.runLater(() -> {
                        clubNotes.removeIf(n -> n.getNoteID().equals(noteID));
                    });

                    cacheManager.saveNotes(new ArrayList<>(clubNotes));
                });
    }

    // ==================== REPLIES ====================

    /**
     * Fetches replies for a note.
     *
     * @param clubID the club's ID
     * @param promptID the prompt's ID
     * @param noteID the note's ID
     * @return CompletableFuture that completes when replies are loaded
     */
    public CompletableFuture<Void> fetchReplies(Long clubID, Long promptID, Long noteID) {
        return apiClient.get("/api/clubs/" + clubID + "/discussions/" + promptID + "/notes/" + noteID + "/replies?page=0&size=100",
                        new TypeReference<PageResponse<Reply>>() {})
                .thenAccept(page -> {
                    Platform.runLater(() -> {
                        replies.clear();
                        replies.addAll(page.getContent());
                    });

                    cacheManager.saveReplies(new ArrayList<>(replies));
                });
    }

    /**
     * Creates a reply to a note.
     *
     * @param clubID the club's ID
     * @param promptID the prompt's ID
     * @param noteID the note's ID
     * @param content reply content
     * @return CompletableFuture with the created reply
     */
    public CompletableFuture<Reply> createReply(Long clubID, Long promptID, Long noteID, String content) {
        return apiClient.post("/api/clubs/" + clubID + "/discussions/" + promptID + "/notes/" + noteID + "/replies",
                        content, Reply.class)
                .thenApply(reply -> {
                    Platform.runLater(() -> {
                        replies.add(reply);
                    });

                    cacheManager.saveReplies(new ArrayList<>(replies));
                    return reply;
                });
    }

    /**
     * Updates a reply.
     *
     * @param clubID the club's ID
     * @param promptID the prompt's ID
     * @param noteID the note's ID
     * @param replyID the reply's ID
     * @param content updated reply content
     * @return CompletableFuture with the updated reply
     */
    public CompletableFuture<Reply> updateReply(Long clubID, Long promptID, Long noteID, Long replyID, String content) {
        return apiClient.put("/api/clubs/" + clubID + "/discussions/" + promptID + "/notes/" + noteID + "/replies/" + replyID,
                        content, Reply.class)
                .thenApply(reply -> {
                    Platform.runLater(() -> {
                        // Replace old reply with updated one
                        replies.removeIf(r -> r.getNoteID().equals(replyID));
                        replies.add(reply);
                    });

                    cacheManager.saveReplies(new ArrayList<>(replies));
                    return reply;
                });
    }

    /**
     * Deletes a reply.
     *
     * @param clubID the club's ID
     * @param promptID the prompt's ID
     * @param noteID the note's ID
     * @param replyID the reply's ID
     * @return CompletableFuture that completes when reply is deleted
     */
    public CompletableFuture<Void> deleteReply(Long clubID, Long promptID, Long noteID, Long replyID) {
        return apiClient.delete("/api/clubs/" + clubID + "/discussions/" + promptID + "/notes/" + noteID + "/replies/" + replyID)
                .thenAccept(v -> {
                    Platform.runLater(() -> {
                        replies.removeIf(r -> r.getNoteID().equals(replyID));
                    });

                    cacheManager.saveReplies(new ArrayList<>(replies));
                });
    }

    // ==================== GETTERS FOR OBSERVABLE LISTS ====================

    public ObservableList<Club> getUserClubs() {
        return FXCollections.unmodifiableObservableList(userClubs);
    }

    public ObservableList<Meeting> getMeetings() {
        return FXCollections.unmodifiableObservableList(meetings);
    }

    public ObservableList<Meeting> getUserMeetings() {
        return FXCollections.unmodifiableObservableList(userMeetings);
    }

    public ObservableList<DiscussionPrompt> getDiscussions() {
        return FXCollections.unmodifiableObservableList(discussions);
    }

    public ObservableList<Note> getClubNotes() {
        return FXCollections.unmodifiableObservableList(clubNotes);
    }

    public ObservableList<Reply> getReplies() {
        return FXCollections.unmodifiableObservableList(replies);
    }

    // ==================== UTILITY ====================

    /**
     * Clears all club data from memory and cache.
     */
    public void clearAllData() {
        Platform.runLater(() -> {
            userClubs.clear();
            meetings.clear();
            discussions.clear();
            clubNotes.clear();
            replies.clear();
        });

        // Clear cache (selective clearing for club data)
        cacheManager.saveMeetings(new ArrayList<>());
        cacheManager.savePrompts(new ArrayList<>());
        cacheManager.saveNotes(new ArrayList<>());
        cacheManager.saveReplies(new ArrayList<>());
    }
}