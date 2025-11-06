package com.litclub.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litclub.Backend.construct.library.book.BookStatus;
import com.litclub.Backend.entity.compositeKey.UserBookID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents the association between a {@link User} and a {@link Book},
 * defining the user's reading relationship and personal engagement data.
 *
 * <p>This entity serves as a junction table between {@code users} and {@code books},
 * enriched with metadata such as reading status, user rating, and reading dates.
 * It forms the backbone of each member’s personal library in LitClub.</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #user} (Many-to-One):</strong> The user who owns or has read the book.</li>
 *   <li><strong>{@link #book} (Many-to-One):</strong> The book being tracked in the user’s library.</li>
 * </ul>
 *
 * <p><strong>Core Attributes:</strong></p>
 * <ul>
 *   <li><strong>{@link #status}:</strong> Defines the user's relationship to the book —
 *       e.g., reading, want to read, read, or did not finish — represented by {@link BookStatus}.</li>
 *   <li><strong>{@link #rating}:</strong> Optional numeric rating given by the user for this book. Inherited from
 *   {@link Review}</li>
 *   <li><strong>{@link #dateStarted}:</strong> The date the user began reading the book.</li>
 *   <li><strong>{@link #dateFinished}:</strong> The date the user completed reading the book.</li>
 *   <li><strong>{@link #createdAt}:</strong> Timestamp of when this record was created.</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>The composite key {@link com.litclub.Backend.entity.compositeKey.UserBookID}
 *       ensures a unique record per user-book pair.</li>
 *   <li>The {@link org.hibernate.annotations.CreationTimestamp @CreationTimestamp}
 *       annotation automatically initializes {@link #createdAt} upon persistence.</li>
 *   <li>Updates to {@link #status}, {@link #rating}, or reading dates
 *       reflect ongoing user activity and can inform recommendations or reading stats.</li>
 * </ul>
 *
 * @see Book
 * @see User
 * @see BookStatus
 * @see com.litclub.Backend.entity.compositeKey.UserBookID
 */

@Entity
@Table(name = "user_books")
@Getter @Setter
public class UserBook {

    @EmbeddedId
    private UserBookID userBookID;

    @MapsId("userID")
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @MapsId("bookID")
    @ManyToOne
    @JoinColumn(name = "book_id")
    @JsonIgnore
    private Book book;

    /**
     * Specifies {@link User}'s relationship with a {@link Book}.
     * Value defined by an {@link BookStatus} enum
     */
    @Column(length = 30)
    private BookStatus status;

    @Column
    private Integer rating;

    @Column(name = "date_started")
    private LocalDate dateStarted;

    @Column(name = "date_finished")
    private LocalDate dateFinished;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
