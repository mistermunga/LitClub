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
public class MeetingAttendeeID implements Serializable {

    @Column(name = "meeting_id")
    private Long meetingID;

    @Column(name = "user_id")
    private Long userID;

}
