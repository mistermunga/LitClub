package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Note;
import com.litclub.Backend.entity.Reply;
import com.litclub.Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing {@link Reply} entities.
 *
 * <p>Provides query methods for retrieving replies by parent note, user,
 * and combinations thereof. Supports both direct and nested reply queries.</p>
 */
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    /**
     * Finds all direct replies to a specific parent note.
     *
     * @param parentNote the parent note
     * @return list of direct replies, empty if none exist
     */
    List<Reply> findAllByParentNote(Note parentNote);

    /**
     * Finds all replies created by a specific user.
     *
     * @param user the user who created the replies
     * @return list of replies by the user, empty if none exist
     */
    List<Reply> findAllByUser(User user);

    /**
     * Finds all replies created by a specific user on a specific parent note.
     *
     * @param user the user who created the replies
     * @param parentNote the parent note
     * @return list of replies by the user on this note, empty if none exist
     */
    List<Reply> findAllByUserAndParentNote(User user, Note parentNote);

    /**
     * Checks if a parent note has any replies.
     *
     * @param parentNote the parent note to check
     * @return true if at least one reply exists
     */
    boolean existsByParentNote(Note parentNote);

    /**
     * Checks if a specific user has replied to a specific note.
     *
     * @param parentNote the parent note
     * @param user the user to check
     * @return true if the user has at least one reply to this note
     */
    boolean existsByParentNoteAndUser(Note parentNote, User user);

    /**
     * Counts the number of direct replies to a parent note.
     *
     * @param parentNote the parent note
     * @return count of direct replies
     */
    int countByParentNote(Note parentNote);

    /**
     * Counts the total number of replies created by a user.
     *
     * @param user the user
     * @return count of replies by the user
     */
    int countByUser(User user);

    /**
     * Finds replies containing specific text in their content (case-insensitive).
     *
     * <p>Because {@code content} is stored as a {@code @Lob}, this query uses
     * a manual {@code CAST} to allow case-insensitive searches.</p>
     *
     * @param content the search text
     * @return list of matching replies, empty if none found
     */
    @Query("""
        SELECT r FROM Reply r
        WHERE LOWER(CAST(r.content AS string)) LIKE LOWER(CONCAT('%', :content, '%'))
       """)
    List<Reply> findByContentContainingIgnoreCase(@Param("content") String content);

    /**
     * Finds replies by a specific user containing specific text (case-insensitive).
     *
     * <p>Because {@code content} is stored as a {@code @Lob}, this query uses
     * a manual {@code CAST} to allow case-insensitive searches.</p>
     *
     * @param user the user who created the replies
     * @param content the search text
     * @return list of matching replies, empty if none found
     */
    @Query("""
        SELECT r FROM Reply r
        WHERE r.user = :user
          AND LOWER(CAST(r.content AS string)) LIKE LOWER(CONCAT('%', :content, '%'))
       """)
    List<Reply> findByUserAndContentContainingIgnoreCase(@Param("user") User user, @Param("content") String content);

    /**
     * Finds all replies to notes created by a specific user.
     *
     * <p>This query finds replies where the parent note's author is the specified user.
     * Useful for finding replies on a user's notes.</p>
     *
     * @param user the user whose notes should be checked for replies
     * @return list of replies on the user's notes
     */
    @Query("""
        SELECT r FROM Reply r
        WHERE r.parentNote.user = :user
       """)
    List<Reply> findRepliesOnNotesCreatedBy(@Param("user") User user);

    /**
     * Finds the most recent replies across the system.
     *
     * @param limit the maximum number of replies to return
     * @return list of recent replies, ordered by creation date descending
     */
    @Query("""
        SELECT r FROM Reply r
        ORDER BY r.createdAt DESC
        LIMIT :limit
       """)
    List<Reply> findRecentReplies(@Param("limit") int limit);

    /**
     * Finds the most recent replies to a specific note.
     *
     * @param parentNote the parent note
     * @param limit the maximum number of replies to return
     * @return list of recent replies to this note, ordered by creation date descending
     */
    @Query("""
        SELECT r FROM Reply r
        WHERE r.parentNote = :parentNote
        ORDER BY r.createdAt DESC
        LIMIT :limit
       """)
    List<Reply> findRecentRepliesForNote(@Param("parentNote") Note parentNote, @Param("limit") int limit);
}