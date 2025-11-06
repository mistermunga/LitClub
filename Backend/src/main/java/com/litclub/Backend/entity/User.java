package com.litclub.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litclub.Backend.security.roles.GlobalRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user account in the LitClub system.
 *
 * <p>A {@code User} is the primary actor in the domain model: they create and join clubs,
 * add books, post notes and replies, schedule meetings, write reviews, and participate in
 * discussions. This entity stores identity information, authentication data (hashed), and
 * cross-cutting role assignments (global roles).</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #memberships} (One-to-Many):</strong> The set of {@link ClubMembership}
 *       records linking this user to clubs and defining per-club roles and membership metadata.</li>
 *   <li><strong>Inverse Relationships (referenced elsewhere):</strong> Many other entities
 *       reference {@code User} (for example {@link com.litclub.Backend.entity.Note},
 *       {@link com.litclub.Backend.entity.Review}, {@link com.litclub.Backend.entity.Meeting},
 *       {@link com.litclub.Backend.entity.MeetingAttendee}, {@link com.litclub.Backend.entity.UserBook}),
 *       which model the user's authored content, attendance, library, and activity.</li>
 * </ul>
 *
 * <p><strong>Core Attributes:</strong></p>
 * <ul>
 *   <li><strong>{@link #userID}:</strong> Primary key.</li>
 *   <li><strong>{@link #username}:</strong> Unique identifier for login and display.</li>
 *   <li><strong>{@link #passwordHash}:</strong> Secure password hash (never store plain text).</li>
 *   <li><strong>{@link #firstName} / {@link #secondName} / {@link #email}:</strong> Profile and contact fields.</li>
 *   <li><strong>{@link #globalRoles}:</strong> Set of {@link com.litclub.Backend.security.roles.GlobalRole}
 *       values granting elevated privileges (e.g., {@code ADMINISTRATOR}).</li>
 *   <li><strong>{@link #createdAt}:</strong> Account creation timestamp.</li>
 * </ul>
 *
 * <p><strong>Lifecycle & Persistence Notes:</strong></p>
 * <ul>
 *   <li>The {@link org.hibernate.annotations.CreationTimestamp @CreationTimestamp} annotation
 *       automatically populates {@link #createdAt} when the user is first persisted.</li>
 *   <li>{@link #memberships} is mapped with {@link jakarta.persistence.CascadeType#ALL} and
 *       {@link jakarta.persistence.FetchType#LAZY}, so membership lifecycle events are propagated
 *       from the user and loaded on demand.</li>
 *   <li>The constructor convenience method automatically assigns the {@code USER} global role,
 *       and conditionally adds {@code ADMINISTRATOR} when requested — ensure higher-level role
 *       assignments are audited and validated at the service layer.</li>
 * </ul>
 *
 * @see com.litclub.Backend.security.roles.GlobalRole
 * @see ClubMembership
 * @see com.litclub.Backend.entity.Note
 * @see com.litclub.Backend.entity.Review
 * @see com.litclub.Backend.entity.Meeting
 * @see com.litclub.Backend.entity.UserBook
 */

@Entity
@Table(name = "users")
@Getter @Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userID;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore
    @Column
    private String passwordHash;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "second_name")
    private String secondName;

    @Column
    private String email;

    @ElementCollection(targetClass = GlobalRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_global_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<GlobalRole> globalRoles = new HashSet<>();

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ClubMembership> memberships = new HashSet<>();

    public User () {}

    public User (
            String username,
            String firstName,
            String surname,
            String email,
            boolean isAdmin
    ) {

        this.username = username;
        this.firstName = firstName;
        this.secondName = surname;
        this.email = email;
        this.globalRoles.add(GlobalRole.USER);

        if (isAdmin) {this.globalRoles.add(GlobalRole.ADMINISTRATOR);}

    }
}
