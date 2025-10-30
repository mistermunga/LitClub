package com.litclub.Backend.entity;

import com.litclub.Backend.entity.compositeKey.RegisterID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
/**
 * An associative entity representing actual attendance records for a {@link Meeting}.
 *
 * <p>{@code MeetingRegister} links a {@link User} to a {@link Meeting} to record whether
 * they attended, arrived late, or were excused. This entity complements
 * {@link MeetingAttendee}, which tracks planned attendance, by serving as a factual
 * record of who participated.</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #meeting} (Many-to-One):</strong> The meeting being recorded.
 *       Each meeting can have multiple attendance records — one per participant.</li>
 *   <li><strong>{@link #user} (Many-to-One):</strong> The member whose attendance is logged.</li>
 * </ul>
 *
 * <p><strong>Core Attributes:</strong></p>
 * <ul>
 *   <li><strong>{@link #attended}:</strong> {@code true} if the user attended the meeting.</li>
 *   <li><strong>{@link #late}:</strong> {@code true} if the user attended but arrived late.</li>
 *   <li><strong>{@link #excused}:</strong> {@code true} if the absence was excused by a moderator or admin.</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>Uses a composite key {@link com.litclub.Backend.entity.compositeKey.RegisterID}
 *       combining {@link #meeting} and {@link #user}, ensuring one attendance record per user per meeting.</li>
 *   <li>Whether a register is required at all is configured through {@link com.litclub.Backend.config.ConfigurationManager}
 *   at the club level.</li>
 *   <li>Registers are typically populated post-meeting by moderators or designated attendance officers.</li>
 * </ul>
 *
 * @see Meeting
 * @see User
 * @see com.litclub.Backend.entity.compositeKey.RegisterID
 * @see com.litclub.Backend.config.ConfigurationManager
 * @see MeetingAttendee
 */

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
