package com.litclub.Backend.entity.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite primary key for {@link com.litclub.Backend.entity.ClubMembership}.
 *
 * <p>Represents the unique association between a Club and a Member.</p>
 */
@Embeddable
@EqualsAndHashCode
@Getter @Setter
public class ClubMembershipID implements Serializable {

    @Column(name = "club_id")
    private Long clubID;

    @Column(name = "member_id")
    private Long memberID;

    public ClubMembershipID() {}

    public ClubMembershipID(Long clubID, Long memberID) {
        this.clubID = clubID;
        this.memberID = memberID;
    }
}
