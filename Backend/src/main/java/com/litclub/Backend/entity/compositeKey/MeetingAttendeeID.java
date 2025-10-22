package com.litclub.Backend.entity.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class MeetingAttendeeID implements Serializable {

    @Column(name = "meeting_id")
    private Long meetingID;

    @Column(name = "user_id")
    private Long userID;

}
