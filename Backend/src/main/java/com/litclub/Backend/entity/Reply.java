package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a reply to another {@link Note}.
 *
 * <p>{@code Reply} extends {@link Note}, inheriting its core structure while
 * explicitly linking to a parent note through {@link #parentNote}. Replies serve
 * as threaded responses or discussions under an existing note, allowing users to
 * engage directly with one another’s ideas.</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #parentNote} (Many-to-One):</strong> The note to which this
 *       reply belongs. Each reply is associated with exactly one parent note,
 *       while a note may have multiple replies.</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>Before persisting, the {@link #clearFields()} method ensures that
 *       reply-specific constraints are enforced:
 *       <ul>
 *         <li>Clears book, club, and discussion references, since replies
 *             exist only within the context of their parent note.</li>
 *         <li>Forces {@code isPrivate} field of {@link Note} to {@code false}, as visibility is
 *             inherited from the parent note rather than managed independently.</li>
 *       </ul>
 *   </li>
 *   <li>Replies participate in the same joined inheritance structure as notes,
 *       using {@link jakarta.persistence.DiscriminatorValue @DiscriminatorValue("REPLY")}
 *       for database-level distinction.</li>
 * </ul>
 * @see Note
 * @see User
 * @see Book
 * @see Club
 */

@Entity
@Table(name = "replies")
@DiscriminatorValue("REPLY")
@Getter @Setter
public class Reply extends Note {

    @ManyToOne(optional = false)
    @JoinColumn(name = "parent_note_id")
    private Note parentNote;

    @SuppressWarnings("override")
    @PrePersist
    public void clearFields() {
        setBook(null);
        setClub(null);
        setDiscussionPrompt(null);
        setPrivate(false);
        super.prePersist();
    }
}


