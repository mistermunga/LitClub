package com.litclub.Backend.entity.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@Getter @Setter
public class ClubMembershipID implements Serializable {

    @Column(name = "club_id")
    private Long clubID;

    @Column(name = "member_id")
    private Long memberID;
}
