package com.litclub.Backend.service.middle;

import com.litclub.Backend.entity.Note;
import com.litclub.Backend.entity.Reply;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.exception.MalformedDTOException;
import com.litclub.Backend.repository.NoteRepository;
import com.litclub.Backend.repository.ReplyRepository;
import com.litclub.Backend.service.low.NoteService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Middle-tier service managing reply creation, retrieval, and threading operations.
 *
 * <p>This service orchestrates reply-related business logic and handles the relationship
 * between replies and their parent notes. Replies are specialized {@link Note} entities
 * that reference other notes, creating threaded discussion trees. This service validates
 * parent note existence but <strong>does not enforce access control</strong> – callers are
 * responsible for authorization.</p>
 *
 * <p><strong>Design Principles:</strong></p>
 * <ul>
 *   <li><strong>Input Validation:</strong> Validates that parent notes exist before creating replies</li>
 *   <li><strong>Entity Trust:</strong> When entities (User, Note) are passed in, they are assumed valid</li>
 *   <li><strong>No Access Control:</strong> Callers must enforce permissions via top-tier services</li>
 *   <li><strong>Nested Support:</strong> Replies can be nested arbitrarily deep (replies to replies)</li>
 * </ul>
 *
 * <p><strong>Tier Position:</strong> Middle tier – depends only on low-tier repositories.</p>
 *
 * <p><strong>Note on Inheritance vs Composition:</strong> This service uses composition rather than
 * inheritance from {@link NoteService} because replies have distinct lifecycle and querying patterns.
 * The {@link Reply} entity already handles the inheritance relationship with {@link Note}.</p>
 *
 * @see Reply
 * @see Note
 * @see ReplyRepository
 * @see NoteRepository
 */
@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final NoteRepository noteRepository;

    public ReplyService(ReplyRepository replyRepository, NoteRepository noteRepository) {
        this.replyRepository = replyRepository;
        this.noteRepository = noteRepository;
    }

    // ====== CREATE ======

    /**
     * Creates a new reply to an existing note.
     *
     * <p>The parent note must exist in the database. Replies inherit constraints from
     * the {@link Reply} entity, particularly that book, club, discussionPrompt, and
     * isPrivate fields are automatically cleared via {@link Reply#clearFields()}.</p>
     *
     * <p><strong>Note:</strong> The {@code user} and {@code parentNote} parameters are
     * assumed to be valid, persisted entities. Callers must validate user permissions
     * and note accessibility before calling this method.</p>
     *
     * @param user the user creating the reply (must be a valid entity)
     * @param parentNote the note being replied to (must be a valid entity)
     * @param content the text content of the reply
     * @return the created {@link Reply} entity
     * @throws EntityNotFoundException if the parent note does not exist in the database
     * @throws MalformedDTOException if content is null or blank
     */
    @Transactional
    public Reply createReply(User user, Note parentNote, String content) {
        if (content == null || content.isBlank()) {
            throw new MalformedDTOException("Reply content cannot be null or blank");
        }

        // Verify parent note exists in database
        if (!noteRepository.existsById(parentNote.getNoteID())) {
            throw new EntityNotFoundException("Parent note with ID " + parentNote.getNoteID() + " not found");
        }

        Reply reply = new Reply();
        reply.setUser(user);
        reply.setParentNote(parentNote);
        reply.setContent(content);

        return replyRepository.save(reply);
    }

    // ====== READ ======

    /**
     * Retrieves a reply by its database ID.
     *
     * @param replyID the reply ID
     * @return the {@link Reply} entity
     * @throws EntityNotFoundException if no reply with the given ID exists
     */
    @Transactional(readOnly = true)
    public Reply getReplyById(Long replyID) {
        if (replyID == null) {
            throw new MalformedDTOException("Reply ID cannot be null");
        }
        return replyRepository.findById(replyID)
                .orElseThrow(() -> new EntityNotFoundException("Reply with ID " + replyID + " not found"));
    }

    /**
     * Retrieves all direct replies to a specific note.
     *
     * <p>This method returns only immediate children, not nested replies.
     * Use {@link #getReplyThread(Note)} to retrieve the full reply tree.</p>
     *
     * <p><strong>Note:</strong> The {@code parentNote} parameter is assumed to be a valid,
     * persisted entity.</p>
     *
     * @param parentNote the note to fetch replies for (must be a valid entity)
     * @return list of direct replies, empty if none exist
     */
    @Transactional(readOnly = true)
    public List<Reply> getRepliesForNote(Note parentNote) {
        return replyRepository.findAllByParentNote(parentNote);
    }

    /**
     * Retrieves the complete reply thread for a note, including nested replies.
     *
     * <p>This method recursively fetches all replies and their replies, building
     * a complete discussion tree. Be cautious with deeply nested threads as this
     * may result in N+1 queries. For large threads, consider using a DTO with
     * fetch joins.</p>
     *
     * @param parentNote the root note to fetch the thread for
     * @return list containing all replies in the thread (flat list, not tree structure)
     */
    @Transactional(readOnly = true)
    public List<Reply> getReplyThread(Note parentNote) {
        List<Reply> directReplies = getRepliesForNote(parentNote);
        List<Reply> allReplies = new java.util.ArrayList<>(directReplies);

        // Recursively fetch replies to replies
        for (Reply reply : directReplies) {
            allReplies.addAll(getReplyThread(reply));
        }

        return allReplies;
    }

    /**
     * Retrieves all replies created by a specific user.
     *
     * <p><strong>Note:</strong> The {@code user} parameter is assumed to be a valid,
     * persisted entity.</p>
     *
     * @param user the user whose replies to retrieve (must be a valid entity)
     * @return list of all replies by the user, empty if none exist
     */
    @Transactional(readOnly = true)
    public List<Reply> getRepliesByUser(User user) {
        return replyRepository.findAllByUser(user);
    }

    /**
     * Retrieves all replies created by a user on a specific note.
     *
     * <p>This includes both direct replies to the note and nested replies within
     * the thread.</p>
     *
     * @param user the user whose replies to retrieve
     * @param parentNote the note to search within
     * @return list of replies by the user on this note
     */
    @Transactional(readOnly = true)
    public List<Reply> getRepliesByUserOnNote(User user, Note parentNote) {
        return replyRepository.findAllByUserAndParentNote(user, parentNote);
    }

    /**
     * Counts the total number of replies (including nested) for a note.
     *
     * @param parentNote the note to count replies for
     * @return total count of all replies in the thread
     */
    @Transactional(readOnly = true)
    public int getReplyCount(Note parentNote) {
        return getReplyThread(parentNote).size();
    }

    /**
     * Counts only the direct replies to a note (not nested).
     *
     * @param parentNote the note to count replies for
     * @return count of direct replies only
     */
    @Transactional(readOnly = true)
    public int getDirectReplyCount(Note parentNote) {
        return replyRepository.countByParentNote(parentNote);
    }

    /**
     * Checks if a note has any replies.
     *
     * @param parentNote the note to check
     * @return true if the note has at least one reply
     */
    @Transactional(readOnly = true)
    public boolean hasReplies(Note parentNote) {
        return replyRepository.existsByParentNote(parentNote);
    }

    /**
     * Retrieves the reply depth (how many levels of nesting) for a specific reply.
     *
     * <p>A direct reply to a note has depth 1, a reply to a reply has depth 2, etc.</p>
     *
     * @param reply the reply to calculate depth for
     * @return the nesting depth (1-based)
     */
    @Transactional(readOnly = true)
    public int getReplyDepth(Reply reply) {
        int depth = 1;
        Note current = reply.getParentNote();

        while (current instanceof Reply) {
            depth++;
            current = ((Reply) current).getParentNote();
        }

        return depth;
    }

    // ====== UPDATE ======

    /**
     * Updates the content of an existing reply.
     *
     * <p>Only the content field can be updated. Parent note references and user
     * ownership cannot be changed after creation.</p>
     *
     * @param replyID the ID of the reply to update
     * @param newContent the new content text
     * @return the updated {@link Reply}
     * @throws EntityNotFoundException if no reply with the given ID exists
     * @throws MalformedDTOException if newContent is null or blank
     */
    @Transactional
    public Reply updateReplyContent(Long replyID, String newContent) {
        if (newContent == null || newContent.isBlank()) {
            throw new MalformedDTOException("Reply content cannot be null or blank");
        }

        Reply reply = getReplyById(replyID);
        reply.setContent(newContent);
        return replyRepository.save(reply);
    }

    // ====== DELETE ======

    /**
     * Deletes a reply by its ID.
     *
     * <p><strong>Cascading Behavior:</strong> This method deletes only the specified reply.
     * Nested replies (children) will become orphaned unless cascade rules are defined
     * in the {@link Reply} entity. Consider using {@link #deleteReplyAndChildren(Long)}
     * for complete thread cleanup.</p>
     *
     * @param replyID the ID of the reply to delete
     * @throws EntityNotFoundException if no reply with the given ID exists
     */
    @Transactional
    public void deleteReply(Long replyID) {
        if (!replyRepository.existsById(replyID)) {
            throw new EntityNotFoundException("Reply with ID " + replyID + " not found");
        }
        replyRepository.deleteById(replyID);
    }

    /**
     * Deletes a reply and all its nested children recursively.
     *
     * <p>This method ensures complete cleanup of reply threads without leaving
     * orphaned nested replies.</p>
     *
     * @param replyID the ID of the reply to delete
     * @throws EntityNotFoundException if no reply with the given ID exists
     */
    @Transactional
    public void deleteReplyAndChildren(Long replyID) {
        Reply reply = getReplyById(replyID);

        // Recursively delete all child replies first
        List<Reply> children = getRepliesForNote(reply);
        for (Reply child : children) {
            deleteReplyAndChildren(child.getNoteID());
        }

        // Delete the reply itself
        replyRepository.deleteById(replyID);
    }

    /**
     * Deletes all direct replies to a specific note.
     *
     * <p><strong>Warning:</strong> This does not recursively delete nested replies.
     * Use {@link #deleteReplyThreadForNote(Note)} for complete thread cleanup.</p>
     *
     * @param parentNote the note whose direct replies should be deleted
     */
    @Transactional
    public void deleteRepliesForNote(Note parentNote) {
        List<Reply> replies = getRepliesForNote(parentNote);
        replyRepository.deleteAll(replies);
    }

    /**
     * Deletes all replies in a thread, including nested replies.
     *
     * <p>This method recursively deletes the entire reply tree under a note,
     * ensuring complete cleanup of the discussion thread.</p>
     *
     * @param parentNote the root note whose entire reply thread should be deleted
     */
    @Transactional
    public void deleteReplyThreadForNote(Note parentNote) {
        List<Reply> directReplies = getRepliesForNote(parentNote);

        for (Reply reply : directReplies) {
            deleteReplyAndChildren(reply.getNoteID());
        }
    }

    /**
     * Deletes all replies created by a specific user across all notes.
     *
     * <p><strong>Cascading Behavior:</strong> If deleted replies have children,
     * those children may become orphaned. Consider your entity cascade configuration.</p>
     *
     * @param user the user whose replies should be deleted
     */
    @Transactional
    public void purgeUserReplies(User user) {
        List<Reply> replies = getRepliesByUser(user);
        replyRepository.deleteAll(replies);
    }
}