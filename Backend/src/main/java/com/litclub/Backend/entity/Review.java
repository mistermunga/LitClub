package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a user-submitted {@code Review} for a {@link Book}.
 *
 * <p>Reviews allow members to rate and share their thoughts on books within the LitClub platform.
 * Each review captures a numerical rating and optional textual feedback, and is tied directly
 * to both the reviewing {@link User} and the reviewed {@link Book}.</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #book} (Many-to-One):</strong> The book being reviewed. A book can have
 *       many reviews, one per user.</li>
 *   <li><strong>{@link #user} (Many-to-One):</strong> The user who authored the review.
 *       A user may write multiple reviews across different books, but typically one per book.</li>
 * </ul>
 *
 * <p><strong>Core Attributes:</strong></p>
 * <ul>
 *   <li><strong>{@link #rating}:</strong> The numeric rating assigned to the book, generally
 *       within a standardized scale (1-10).</li>
 *   <li><strong>{@link #content}:</strong> Optional written commentary elaborating on the user’s
 *       rating and overall impression.</li>
 *   <li><strong>{@link #createdAt}:</strong> Timestamp of when the review was created.</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>The {@link org.hibernate.annotations.CreationTimestamp @CreationTimestamp}
 *       annotation automatically sets {@link #createdAt} at persistence time.</li>
 *   <li>Reviews are used for aggregation and recommendation features, such as
 *       computing average ratings per book or generating personalized reading suggestions.</li>
 * </ul>
 *
 * @see Book
 * @see User
 */

@Entity
@Table(name = "reviews")
@Setter @Getter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewID;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private int rating;

    @Lob
    private String content;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
