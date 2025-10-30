package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a scheduled {@code Meeting} within a {@link Club}.
 *
 * <p>A meeting is a planned event organized by a club to discuss books,
 * share insights, or host live reading sessions. Meetings can occur in
 * person (via {@link #location}) or online (via {@link #link}), and are
 * typically created by a club moderator or administrator.</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #club} (Many-to-One):</strong> The club hosting this meeting.
 *       Each club may have multiple meetings, but every meeting belongs to exactly one club.</li>
 *   <li><strong>{@link #creator} (Many-to-One):</strong> The {@link User} who scheduled
 *       or created the meeting. Typically, this user holds a role of at least
 *       {@code MODERATOR} as defined in {@link com.litclub.Backend.security.roles.ClubRole}.</li>
 * </ul>
 *
 * <p><strong>Core Attributes:</strong></p>
 * <ul>
 *   <li><strong>{@link #title}:</strong> A short, descriptive name for the meeting.</li>
 *   <li><strong>{@link #startTime}/{@link #endTime}:</strong> The scheduled start and end times.</li>
 *   <li><strong>{@link #location}:</strong> A physical meeting place (optional).</li>
 *   <li><strong>{@link #link}:</strong> A virtual meeting URL (optional, e.g. Zoom or Google Meet link).</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>The {@link org.hibernate.annotations.CreationTimestamp @CreationTimestamp}
 *       annotation automatically sets {@link #createdAt} upon persistence.</li>
 *   <li>Meetings are immutable after creation except for administrative updates
 *       such as rescheduling or changing the meeting link.</li>
 *   <li>Club-level access and scheduling permissions are controlled through
 *       {@link com.litclub.Backend.config.ConfigurationManager} and {@link com.litclub.Backend.security.roles.ClubRole}.</li>
 * </ul>
 *
 * @see Club
 * @see User
 * @see com.litclub.Backend.security.roles.ClubRole
 * @see com.litclub.Backend.config.ConfigurationManager
 */

@Entity
@Table(name = "meetings")
@Getter @Setter
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingID;

    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    @Column(name = "title")
    private String title;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column
    private String location;

    @Column
    private String link;

    @ManyToOne
    @JoinColumn
    private User creator;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
