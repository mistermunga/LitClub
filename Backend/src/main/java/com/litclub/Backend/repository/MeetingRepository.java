package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Meeting} entities.
 *
 * <p>Provides query methods for retrieving meetings by club, creator, time ranges,
 * and status (upcoming, past, current). Prioritizes Spring Data JPA method name
 * queries over custom JPQL/SQL, using custom queries only where complex logic or
 * optimization is needed.</p>
 */
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    // ====== BASIC FINDERS ======

    /**
     * Finds a meeting by its database ID.
     *
     * @param meetingID the meeting ID
     * @return optional containing the meeting, or empty if not found
     */
    Optional<Meeting> findByMeetingID(Long meetingID);

    /**
     * Finds all meetings for a specific club.
     *
     * @param club     the club
     * @return list of meetings, empty if none exist
     */
    List<Meeting> findAllByClub(Club club);

    /**
     * Finds all meetings for a specific club.
     *
     * @param club     the club
     * @param pageable pageable argument
     * @return list of meetings, empty if none exist
     */
    Page<Meeting> findAllByClub(Club club, Pageable pageable);

    /**
     * Finds all meetings created by a specific user.
     *
     * @param creator the user who created the meetings
     * @return list of meetings, empty if none exist
     */
    List<Meeting> findAllByCreator(User creator);

    /**
     * Finds all meetings created by a user within a specific club.
     *
     * @param creator the user who created the meetings
     * @param club the club
     * @return list of meetings, empty if none exist
     */
    List<Meeting> findAllByCreatorAndClub(User creator, Club club);

    // ====== TIME-BASED QUERIES ======

    /**
     * Finds upcoming meetings for a club (start time after specified time).
     *
     * @param club the club
     * @param time the reference time (typically current time)
     * @return list of upcoming meetings ordered by start time ascending
     */
    List<Meeting> findByClubAndStartTimeAfterOrderByStartTimeAsc(Club club, LocalDateTime time);

    /**
     * Finds past meetings for a club (end time before specified time).
     *
     * @param club the club
     * @param time the reference time (typically current time)
     * @return list of past meetings ordered by start time descending
     */
    List<Meeting> findByClubAndEndTimeBeforeOrderByStartTimeDesc(Club club, LocalDateTime time);

    /**
     * Finds meetings currently in progress for a club.
     *
     * <p>A meeting is in progress if current time is between start and end times.
     * This query uses method name derivation for clarity.</p>
     *
     * @param club the club
     * @param startBefore current time (for start time comparison)
     * @param endAfter current time (for end time comparison)
     * @return list of meetings currently in progress
     */
    List<Meeting> findByClubAndStartTimeBeforeAndEndTimeAfter(Club club, LocalDateTime startBefore, LocalDateTime endAfter);

    /**
     * Finds meetings scheduled within a date range.
     *
     * @param club the club
     * @param startDate beginning of range (inclusive)
     * @param endDate end of range (inclusive)
     * @return list of meetings with start time in the range
     */
    List<Meeting> findByClubAndStartTimeBetween(Club club, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds meetings by title (case-insensitive partial match).
     *
     * @param title the title or partial title to search for
     * @return list of matching meetings
     */
    List<Meeting> findByTitleContainingIgnoreCase(String title);

    /**
     * Finds meetings by location (case-insensitive partial match).
     *
     * @param location the location or partial location to search for
     * @return list of matching meetings
     */
    List<Meeting> findByLocationContainingIgnoreCase(String location);

    // ====== EXISTENCE & COUNT QUERIES ======

    /**
     * Checks if a club has any meetings.
     *
     * @param club the club to check
     * @return true if at least one meeting exists
     */
    boolean existsByClub(Club club);

    /**
     * Checks if a user has created any meetings.
     *
     * @param creator the user to check
     * @return true if the user has created at least one meeting
     */
    boolean existsByCreator(User creator);

    /**
     * Counts total meetings for a club.
     *
     * @param club the club
     * @return total number of meetings
     */
    long countByClub(Club club);

    /**
     * Counts upcoming meetings for a club.
     *
     * @param club the club
     * @param time the reference time (typically current time)
     * @return number of meetings with start time after the specified time
     */
    long countByClubAndStartTimeAfter(Club club, LocalDateTime time);

    /**
     * Counts past meetings for a club.
     *
     * @param club the club
     * @param time the reference time (typically current time)
     * @return number of meetings with end time before the specified time
     */
    long countByClubAndEndTimeBefore(Club club, LocalDateTime time);

    // ====== CUSTOM JPQL QUERIES (Used where method names become unwieldy) ======

    /**
     * Finds meetings scheduled for a specific date (ignoring time).
     *
     * <p>Uses JPQL for date truncation logic which would be verbose with method names.</p>
     *
     * @param club the club
     * @param date the date to search for
     * @return list of meetings on the specified date
     */
    @Query("""
        SELECT m FROM Meeting m\s
        WHERE m.club = :club\s
        AND FUNCTION('DATE', m.startTime) = FUNCTION('DATE', :date)
        ORDER BY m.startTime ASC
       \s""")
    List<Meeting> findMeetingsOnDate(@Param("club") Club club, @Param("date") LocalDateTime date);

    /**
     * Finds the most recently created meetings across all clubs.
     *
     * <p>Uses JPQL for complex ordering and limiting which isn't well-supported
     * by method name queries.</p>
     *
     * @param limit maximum number of meetings to return
     * @return list of recent meetings ordered by creation time descending
     */
    @Query("""
        SELECT m FROM Meeting m\s
        ORDER BY m.createdAt DESC\s
        LIMIT :limit
       \s""")
    List<Meeting> findRecentlyCreatedMeetings(@Param("limit") int limit);

    /**
     * Finds meetings with no RSVPs yet.
     *
     * <p>Uses JPQL with LEFT JOIN to identify meetings without attendee records.
     * This is more efficient than checking in application code.</p>
     *
     * @param club the club
     * @return list of meetings with no RSVP records
     */
    @Query("""
        SELECT m FROM Meeting m\s
        LEFT JOIN MeetingAttendee ma ON ma.meeting = m\s
        WHERE m.club = :club\s
        AND ma.meetingAttendeeID IS NULL
       \s""")
    List<Meeting> findMeetingsWithNoRSVPs(@Param("club") Club club);

    /**
     * Finds meetings with low attendance (fewer RSVPs than threshold).
     *
     * <p>Uses JPQL GROUP BY and HAVING for aggregate comparison, which would
     * require multiple queries with method name approach.</p>
     *
     * @param club the club
     * @param threshold minimum number of RSVPs
     * @return list of meetings with fewer RSVPs than threshold
     */
    @Query("""
        SELECT m FROM Meeting m\s
        LEFT JOIN MeetingAttendee ma ON ma.meeting = m\s
        WHERE m.club = :club\s
        GROUP BY m.meetingID\s
        HAVING COUNT(ma) < :threshold
       \s""")
    List<Meeting> findMeetingsWithLowAttendance(@Param("club") Club club, @Param("threshold") long threshold);

    /**
     * Finds upcoming meetings a user has not yet RSVPed to within their clubs.
     *
     * <p>Complex multi-table query using JPQL for clarity and performance.
     * Identifies meetings in the user's clubs where they haven't created an
     * attendee record and the meeting hasn't started yet.</p>
     *
     * @param user the user
     * @param now current time
     * @return list of un-RSVPed upcoming meetings in user's clubs
     */
    @Query("""
        SELECT m FROM Meeting m\s
        WHERE m.club IN (
            SELECT cm.club FROM ClubMembership cm WHERE cm.member = :user
        )\s
        AND m.startTime > :now\s
        AND NOT EXISTS (
            SELECT ma FROM MeetingAttendee ma\s
            WHERE ma.meeting = m AND ma.user = :user
        )
        ORDER BY m.startTime ASC
       \s""")
    List<Meeting> findUpcomingUnRSVPedMeetingsForUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Finds meetings with conflicts (overlapping times) for a club.
     *
     * <p>Uses JPQL to detect time overlaps, which is complex logic better
     * expressed in SQL than method names. Returns meetings that overlap with
     * the specified time range.</p>
     *
     * @param club the club
     * @param startTime the start of the time range to check
     * @param endTime the end of the time range to check
     * @return list of meetings with overlapping times
     */
    @Query("""
        SELECT m FROM Meeting m\s
        WHERE m.club = :club\s
        AND (
            (m.startTime < :endTime AND m.endTime > :startTime)
        )
       \s""")
    List<Meeting> findConflictingMeetings(
            @Param("club") Club club,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Finds clubs ranked by total meeting count.
     *
     * <p>Uses JPQL GROUP BY and ORDER BY for aggregate statistics that would
     * require multiple queries with repository methods.</p>
     *
     * @param limit maximum number of clubs to return
     * @return list of clubs ordered by meeting count descending
     */
    @Query("""
        SELECT m.club, COUNT(m) as meetingCount\s
        FROM Meeting m\s
        GROUP BY m.club\s
        ORDER BY meetingCount DESC\s
        LIMIT :limit
       \s""")
    List<Object[]> findMostActiveClusByMeetingCount(@Param("limit") int limit);
}
