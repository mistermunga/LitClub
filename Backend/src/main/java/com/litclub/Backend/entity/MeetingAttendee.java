package com.litclub.Backend.entity;

import com.litclub.Backend.entity.compositeKey.MeetingAttendeeID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_attendees")
@Getter @Setter
public class MeetingAttendee {

    @EmbeddedId
    private MeetingAttendeeID meetingAttendeeID;

    @ManyToOne
    @MapsId("meetingID")
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne
    @MapsId("userID")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rsvp_status")
    private String rsvpStatus;  // attending, maybe, pass

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
