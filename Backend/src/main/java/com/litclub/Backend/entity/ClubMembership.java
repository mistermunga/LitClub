package com.litclub.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litclub.Backend.entity.compositeKey.ClubMembershipID;
import com.litclub.Backend.security.roles.ClubRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * An associative entity that models the relationship between {@link Club} and {@link User} entities.
 *
 * <p>This entity represents a user's membership in a specific club, encapsulating additional
 * attributes about the relationship, such as the member's {@link ClubRole roles} within the club
 * and the timestamp of when they joined.</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #club} (Many-to-One):</strong> Each membership is associated with exactly one {@link Club}.
 *       A club can have many memberships, one for each member that joins.</li>
 *   <li><strong>{@link #member} (Many-to-One):</strong> Each membership references a single {@link User}.
 *       A user can belong to multiple clubs, resulting in multiple {@code ClubMembership} records.</li>
 *   <li><strong>{@link #roles} (ElementCollection):</strong> Each membership can have one or more {@link ClubRole}s
 *       defining the member’s permissions and responsibilities within that club (e.g., ADMIN, MEMBER, MODERATOR).</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>The composite key {@link ClubMembershipID} ensures that each (club, member) pair is unique —
 *       a user cannot join the same club more than once.</li>
 *   <li>The {@link org.hibernate.annotations.CreationTimestamp @CreationTimestamp} annotation automatically
 *       sets {@link #joinedAt} when the membership is first persisted.</li>
 *   <li>Deletion of a {@link Club} or {@link User} may cascade or trigger orphan removal depending on
 *       the cascade configuration in the parent entity mappings.</li>
 * </ul>
 *
 * @see Club
 * @see User
 * @see ClubRole
 * @see ClubMembershipID
 */

@Entity
@Table(name = "club_memberships")
@Getter @Setter
public class ClubMembership {

    @EmbeddedId
    private ClubMembershipID clubMembershipID = new ClubMembershipID();

    @ManyToOne
    @MapsId("memberID")
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnore
    private User member;

    @ManyToOne
    @MapsId("clubID")
    @JoinColumn(name = "club_id", nullable = false)
    @JsonIgnore
    private Club club;

    @ElementCollection(targetClass = ClubRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "club_membership_roles",
            joinColumns = {
                    @JoinColumn(name = "club_id", referencedColumnName = "club_id"),
                    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
            })
    @Enumerated(EnumType.STRING)
    private Set<ClubRole> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
