package com.litclub.Backend.entity;

import com.litclub.Backend.entity.compositeKey.RegisterID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "meeting_register")
@Getter @Setter
public class MeetingRegister {

    @EmbeddedId
    private RegisterID registerID;

    @ManyToOne
    @MapsId("meetingID")
    @JoinColumn(nullable = false, name = "meeting_id")
    private Meeting meeting;

    @ManyToOne
    @MapsId("userID")
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column
    private boolean attended;

    @Column
    private boolean late;

    @Column
    private boolean excused;


}
