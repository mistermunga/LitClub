package com.litclub.Backend.entity.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite primary key for {@link com.litclub.Backend.entity.MeetingRegister}.
 *
 * <p>Represents a unique attendance record of a User in a Meeting’s register.</p>
 */
@Embeddable
@EqualsAndHashCode
@Getter @Setter
public class RegisterID implements Serializable {

    @Column(name = "meeting_id")
    private Long meetingID;

    @Column(name = "user_id")
    private Long userID;

}
