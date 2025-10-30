package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a club within the LitClub platform.
 *
 * <p>A {@code Club} serves as a community space where {@link User}s can join,
 * share discussions, and participate in literary activities. Clubs are a core concept
 * in LitClub, defining the organizational and social boundaries of interaction.</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #creator} (Many-to-One):</strong> Each club is created and owned by a single {@link User}.
 *       The creator typically holds elevated permissions (e.g., administrative or moderator roles).</li>
 *   <li><strong>{@link ClubMembership} (One-to-Many, inverse side):</strong> Memberships link users to clubs.
 *       A club can have many members through {@link ClubMembership} entities.</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>The {@link org.hibernate.annotations.CreationTimestamp @CreationTimestamp} annotation
 *       automatically initializes {@link #createdAt} when the club is first persisted.</li>
 *   <li>Deletion or modification of a club may cascade to related memberships,
 *       depending on the cascade configuration defined in the {@link ClubMembership} entity.</li>
 * </ul>
 *
 * <p><strong>Additional Details:</strong></p>
 * <ul>
 *   <li>Club configuration options and defaults are managed by
 *       {@link com.litclub.Backend.config.ConfigurationManager}.</li>
 *   <li>Role assignments and permission scopes for club participants are defined in
 *       {@link com.litclub.Backend.security.roles.ClubRole}.</li>
 * </ul>
 *
 * @see User
 * @see ClubMembership
 * @see com.litclub.Backend.service.middle.ClubService
 * @see com.litclub.Backend.security.roles.ClubRole
 * @see com.litclub.Backend.config.ConfigurationManager
 */

@Entity
@Table(name = "clubs")
@Getter @Setter
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long clubID;

    @Column(nullable = false, name = "club_name")
    private String clubName;

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
