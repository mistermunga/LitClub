package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a discussion prompt posted within a {@link Club}.
 *
 * <p>A {@code DiscussionPrompt} is a message or topic created by a club member—
 * typically a moderator or higher (as defined in {@link com.litclub.Backend.security.roles.ClubRole})—
 * to encourage discussion among club members. Other users respond to a prompt
 * through their {@code Note} entities, forming the basis of club discussions.</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #poster} (Many-to-One):</strong> References the {@link User}
 *       who created the prompt. This user must hold a {@link com.litclub.Backend.security.roles.ClubRole}
 *       of at least {@code MODERATOR}, unless overridden by club configuration.</li>
 *   <li><strong>{@link #club} (Many-to-One):</strong> Associates the prompt with a specific {@link Club}.
 *       Each club can have many prompts, but each prompt belongs to exactly one club.</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>The {@link org.hibernate.annotations.CreationTimestamp @CreationTimestamp}
 *       annotation automatically sets {@link #postedAt} when the prompt is first persisted.</li>
 *   <li>Access control and creation permissions for prompts are determined by
 *       {@link com.litclub.Backend.config.ConfigurationManager}, which defines who
 *       (by role or rank) can post within a given club.</li>
 *   <li>Associated user responses are modeled as {@code Note} entities, linked externally
 *       via a foreign key or prompt reference (not defined here).</li>
 * </ul>
 *
 * @see Club
 * @see User
 * @see com.litclub.Backend.security.roles.ClubRole
 * @see com.litclub.Backend.config.ConfigurationManager
 * @see com.litclub.Backend.entity.Note
 */

@Entity
@Table(name = "discussion_prompts")
@Getter @Setter
public class DiscussionPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promptID;

    @ManyToOne
    @JoinColumn(name = "poster_id", nullable = false)
    private User poster;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(nullable = false)
    private String prompt;

    @CreationTimestamp
    @Column(name = "posted_at")
    private LocalDateTime postedAt;

}
