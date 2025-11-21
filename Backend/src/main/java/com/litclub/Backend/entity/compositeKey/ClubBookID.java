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
public class ClubBookID implements Serializable {

    @Column(name = "club_id")
    private Long clubID;

    @Column(name = "book_id")
    private Long bookID;

    public ClubBookID() {}

    public ClubBookID(Long clubID, Long bookID) {
        this.clubID = clubID;
        this.bookID = bookID;
    }

}
