package com.litclub.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litclub.Backend.entity.compositeKey.ClubBookID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_books")
@Getter @Setter
public class ClubBook {

    @EmbeddedId
    private ClubBookID clubBookID;

    @ManyToOne
    @MapsId("clubID")
    @JoinColumn(name = "club_id", nullable = false)
    @JsonIgnore
    private Club club;

    @ManyToOne
    @MapsId("bookID")
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore
    private Book book;

    @Column(name = "is_valid", nullable = false)
    private boolean valid;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ClubBook() {}

    public ClubBook(Club club, Book book) {
        this.club = club;
        this.book = book;
        this.valid = true;
        this.clubBookID = new ClubBookID(club.getClubID(), book.getBookID());
    }

}
