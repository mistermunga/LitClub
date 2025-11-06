package com.litclub.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user-created note — a piece of content linked to a {@link Book},
 * optionally shared within a {@link Club} or tied to a {@link DiscussionPrompt}.
 *
 * <p>Notes are the central means of expression in LitClub: users create them to
 * reflect on books, respond to prompts, or share insights with their club members.
 * Notes can be either public (shared within a club or discussion) or private (visible
 * only to the author).</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #book} (Many-to-One):</strong> The book this note discusses.
 *       Every note must be associated with one book.</li>
 *   <li><strong>{@link #user} (Many-to-One):</strong> The author of the note. A user can
 *       have many notes, both public and private.</li>
 *   <li><strong>{@link #club} (Many-to-One, optional):</strong> The club context in which
 *       this note was posted. May be {@code null} for private notes.</li>
 *   <li><strong>{@link #discussionPrompt} (Many-to-One, optional):</strong> Links the note
 *       to a {@link DiscussionPrompt} if it’s a response to one. May be {@code null}
 *       if the note is independent or private.</li>
 *   <li><strong>{@link #replies} (One-to-Many):</strong> A collection of {@link Reply}
 *       entities representing comments or follow-ups to this note.</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>The {@link org.hibernate.annotations.CreationTimestamp @CreationTimestamp}
 *       annotation automatically sets {@link #createdAt} when the note is persisted.</li>
 *   <li>During {@link jakarta.persistence.PrePersist @PrePersist}, if {@link #isPrivate}
 *       is {@code true}, all club-related associations ({@link #club}, {@link #discussionPrompt})
 *       are cleared to prevent unintended exposure in shared contexts.</li>
 *   <li>The entity uses {@link jakarta.persistence.Inheritance InheritanceType#JOINED},
 *       allowing subclasses such as {@link Reply} to extend note behavior while sharing
 *       core fields.</li>
 * </ul>
 *
 * @see Book
 * @see Club
 * @see User
 * @see DiscussionPrompt
 * @see Reply
 */

@Entity
@Table(name = "notes")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noteID;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    @ManyToOne
    @JoinColumn(name = "discussion_id")
    private DiscussionPrompt discussionPrompt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column
    private boolean isPrivate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "parentNote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Reply> replies = new HashSet<>();


    @PrePersist
    public void prePersist() {
        if (isPrivate) {
            this.club = null;
            this.discussionPrompt = null;
        }
    }

}
