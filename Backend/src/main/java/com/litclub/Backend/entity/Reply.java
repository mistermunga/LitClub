package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "replies")
@DiscriminatorValue("REPLY")
@Getter @Setter
public class Reply extends Note {

    @ManyToOne(optional = false)
    @JoinColumn(name = "parent_note_id")
    private Note parentNote;

    @SuppressWarnings("override")
    @PrePersist
    public void clearFields() {
        setBook(null);
        setClub(null);
        setDiscussionPrompt(null);
        setPrivate(false);
        super.prePersist();
    }
}


