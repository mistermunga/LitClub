package com.litclub.Backend.entity.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite primary key for {@link com.litclub.Backend.entity.MeetingAttendee}.
 *
 * <p>Uniquely identifies a User’s RSVP record for a specific Meeting.</p>
 */
@Embeddable
@EqualsAndHashCode
@Getter @Setter
public class MeetingAttendeeID implements Serializable {

    @Column(name = "meeting_id")
    private Long meetingID;

    @Column(name = "user_id")
    private Long userID;

}
