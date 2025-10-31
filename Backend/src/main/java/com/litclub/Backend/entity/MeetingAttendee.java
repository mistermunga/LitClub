package com.litclub.Backend.entity;

import com.litclub.Backend.construct.meeting.RsvpStatus;
import com.litclub.Backend.entity.compositeKey.MeetingAttendeeID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * An associative entity representing a user's RSVP response for a {@link Meeting}.
 *
 * <p>{@code MeetingAttendee} links a {@link User} to a {@link Meeting} in advance of the event,
 * recording the member’s RSVP status (e.g., attending, maybe, or pass). This entity models
 * intended attendance, allowing clubs to plan participation and enforce attendance policies
 * defined in {@link com.litclub.Backend.config.ConfigurationManager}.</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #meeting} (Many-to-One):</strong> The meeting that the user is responding to.
 *       Each meeting can have multiple attendees, one per participating user.</li>
 *   <li><strong>{@link #user} (Many-to-One):</strong> The member submitting the RSVP.
 *       Each user may have multiple {@code MeetingAttendee} records, one for each meeting they’ve been invited to.</li>
 * </ul>
 *
 * <p><strong>Core Attributes:</strong></p>
 * <ul>
 *   <li><strong>{@link #rsvpStatus}:</strong> Textual RSVP indicator, typically one of
 *       {@code "attending"}, {@code "maybe"}, or {@code "pass"}.</li>
 *   <li><strong>{@link #createdAt}:</strong> Timestamp of when the RSVP was recorded.</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>Uses a composite key {@link com.litclub.Backend.entity.compositeKey.MeetingAttendeeID}
 *       combining {@link #meeting} and {@link #user}, ensuring a user can RSVP only once per meeting.</li>
 *   <li>The {@link org.hibernate.annotations.CreationTimestamp @CreationTimestamp} annotation
 *       automatically sets {@link #createdAt} when the RSVP is first persisted.</li>
 *   <li>Club-specific rules, such as whether RSVP responses are required or optional,
 *       are governed by {@link com.litclub.Backend.config.ConfigurationManager} and
 *       enforced at the service level.</li>
 * </ul>
 *
 * @see Meeting
 * @see User
 * @see com.litclub.Backend.entity.compositeKey.MeetingAttendeeID
 * @see com.litclub.Backend.config.ConfigurationManager
 */

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
    private RsvpStatus rsvpStatus;  // attending, maybe, pass

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
