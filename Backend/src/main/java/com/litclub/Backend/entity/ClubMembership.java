package com.litclub.Backend.entity;

import com.litclub.Backend.entity.compositeKey.ClubMembershipID;
import com.litclub.Backend.security.roles.ClubRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "club_memberships")
@Getter @Setter
public class ClubMembership {

    @EmbeddedId
    private ClubMembershipID clubMembershipID;

    @ManyToOne
    @MapsId("memberID")
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne
    @MapsId("clubID")
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ElementCollection(targetClass = ClubRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "club_membership_roles",
            joinColumns = {
                    @JoinColumn(name = "club_id", referencedColumnName = "club_id"),
                    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
            })
    @Enumerated(EnumType.STRING)
    private Set<ClubRole> roles = new HashSet<>();

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    public void onCreate() {
        if (this.joinedAt == null) {
            this.joinedAt = LocalDateTime.now();
        }
    }
}
