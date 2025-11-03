package com.litclub.Backend.service.middle;

import com.litclub.Backend.construct.meeting.RsvpStatus;
import com.litclub.Backend.entity.*;
import com.litclub.Backend.exception.MalformedDTOException;
import com.litclub.Backend.repository.MeetingRepository;
import com.litclub.Backend.service.low.MeetingAttendeeService;
import com.litclub.Backend.service.low.MeetingRegisterService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Middle-tier service managing meeting creation, scheduling, and attendance operations.
 *
 * <p>This service orchestrates meeting-related business logic and delegates attendee management
 * and register operations to low-tier services. It handles meeting lifecycle operations including
 * creation, scheduling, updates, and deletion, while maintaining relationships with attendees
 * and attendance records. This service validates meeting existence but <strong>does not enforce
 * access control</strong> – callers are responsible for authorization.</p>
 *
 * <p><strong>Design Principles:</strong></p>
 * <ul>
 *   <li><strong>Input Validation:</strong> Validates meeting existence and time constraints</li>
 *   <li><strong>Entity Trust:</strong> When entities (User, Club) are passed in, they are assumed valid</li>
 *   <li><strong>No Access Control:</strong> Callers must enforce permissions via top-tier services</li>
 *   <li><strong>Delegation:</strong> Attendee and register operations delegated to low-tier services</li>
 *   <li><strong>Time Awareness:</strong> Provides methods to query past, current, and upcoming meetings</li>
 * </ul>
 *
 * <p><strong>Tier Position:</strong> Middle tier – depends only on low-tier services and repositories.</p>
 *
 * <p><strong>Meeting Lifecycle:</strong></p>
 * <ol>
 *   <li>Meeting created with basic info (title, time, location/link)</li>
 *   <li>Users RSVP via {@link MeetingAttendeeService} (managed through this service)</li>
 *   <li>Meeting occurs</li>
 *   <li>Attendance recorded via {@link MeetingRegisterService} (managed through this service)</li>
 * </ol>
 *
 * @see Meeting
 * @see MeetingAttendee
 * @see MeetingRegister
 * @see MeetingRepository
 * @see MeetingAttendeeService
 * @see MeetingRegisterService
 */
@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingAttendeeService meetingAttendeeService;
    private final MeetingRegisterService meetingRegisterService;

    public MeetingService(MeetingRepository meetingRepository,
                          MeetingAttendeeService meetingAttendeeService,
                          MeetingRegisterService meetingRegisterService) {
        this.meetingRepository = meetingRepository;
        this.meetingAttendeeService = meetingAttendeeService;
        this.meetingRegisterService = meetingRegisterService;
    }

    // ====== CREATE ======

    /**
     * Creates a new meeting for a club.
     *
     * <p>Either location (for in-person) or link (for virtual) should be provided,
     * though both can be set for hybrid meetings. End time must be after start time.</p>
     *
     * <p><strong>Note:</strong> The {@code club} and {@code creator} parameters are
     * assumed to be valid, persisted entities. Callers must validate creator permissions
     * before calling this method.</p>
     *
     * @param club the club hosting the meeting (must be a valid entity)
     * @param creator the user creating the meeting (must be a valid entity)
     * @param title the meeting title
     * @param startTime when the meeting begins
     * @param endTime when the meeting ends
     * @param location physical location (optional, for in-person or hybrid meetings)
     * @param link virtual meeting link (optional, for online or hybrid meetings)
     * @return the created {@link Meeting} entity
     * @throws MalformedDTOException if title is blank, times are null, or end time is before start time
     */
    @Transactional
    public Meeting createMeeting(Club club, User creator, String title,
                                 LocalDateTime startTime, LocalDateTime endTime,
                                 String location, String link) {
        validateMeetingTimes(startTime, endTime);

        if (title == null || title.isBlank()) {
            throw new MalformedDTOException("Meeting title cannot be null or blank");
        }

        Meeting meeting = new Meeting();
        meeting.setClub(club);
        meeting.setCreator(creator);
        meeting.setTitle(title);
        meeting.setStartTime(startTime);
        meeting.setEndTime(endTime);
        meeting.setLocation(location);
        meeting.setLink(link);

        return meetingRepository.save(meeting);
    }

    // ====== READ ======

    /**
     * Retrieves a meeting by its database ID.
     *
     * @param meetingID the meeting ID
     * @return the {@link Meeting} entity
     * @throws EntityNotFoundException if no meeting with the given ID exists
     */
    @Transactional(readOnly = true)
    public Meeting requireById(long meetingID) {
        return meetingRepository.findByMeetingID(meetingID)
                .orElseThrow(() -> new EntityNotFoundException("Meeting with id: " + meetingID + " not found"));
    }

    /**
     * Retrieves all meetings in the system.
     *
     * @return list of all meetings, empty if none exist
     */
    @Transactional(readOnly = true)
    public Page<Meeting> getAllMeetings(Pageable pageable) {
        return meetingRepository.findAll(pageable);
    }

    /**
     * Retrieves all meetings for a specific club.
     *
     * <p><strong>Note:</strong> The {@code club} parameter is assumed to be a valid,
     * persisted entity.</p>
     *
     * @param club the club whose meetings to retrieve (must be a valid entity)
     * @return list of meetings for the club, empty if none exist
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsForClub(Club club) {
        return meetingRepository.findAllByClub(club);
    }

    @Transactional(readOnly = true)
    public Page<Meeting> getMeetingsForClub(Club club, Pageable pageable) {
        return meetingRepository.findAllByClub(club, pageable);
    }

    /**
     * Retrieves all meetings created by a specific user across all clubs.
     *
     * <p><strong>Note:</strong> The {@code creator} parameter is assumed to be a valid,
     * persisted entity.</p>
     *
     * @param creator the user who created the meetings (must be a valid entity)
     * @return list of meetings created by the user, empty if none exist
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByCreator(User creator) {
        return meetingRepository.findAllByCreator(creator);
    }

    /**
     * Retrieves all meetings created by a user within a specific club.
     *
     * @param creator the user who created the meetings
     * @param club the club to filter by
     * @return list of meetings, empty if none exist
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByCreatorAndClub(User creator, Club club) {
        return meetingRepository.findAllByCreatorAndClub(creator, club);
    }

    @Transactional(readOnly = true)
    public List<Meeting> getMeetings(User user, Club club) {
        return meetingAttendeeService.findAllMeetingAttendances(club, user).stream()
                .map(MeetingAttendee::getMeeting)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all upcoming meetings for a club.
     *
     * <p>Upcoming meetings are those with start times after the current moment.</p>
     *
     * @param club the club whose upcoming meetings to retrieve
     * @return list of upcoming meetings ordered by start time, empty if none scheduled
     */
    @Transactional(readOnly = true)
    public List<Meeting> getUpcomingMeetings(Club club) {
        return meetingRepository.findByClubAndStartTimeAfterOrderByStartTimeAsc(club, LocalDateTime.now());
    }

    /**
     * Retrieves all past meetings for a club.
     *
     * <p>Past meetings are those with end times before the current moment.</p>
     *
     * @param club the club whose past meetings to retrieve
     * @return list of past meetings ordered by start time descending, empty if none
     */
    @Transactional(readOnly = true)
    public List<Meeting> getPastMeetings(Club club) {
        return meetingRepository.findByClubAndEndTimeBeforeOrderByStartTimeDesc(club, LocalDateTime.now());
    }

    /**
     * Retrieves meetings currently in progress for a club.
     *
     * <p>A meeting is in progress if the current time is between its start and end times.</p>
     *
     * @param club the club whose current meetings to retrieve
     * @return list of meetings currently in progress, empty if none
     */
    @Transactional(readOnly = true)
    public List<Meeting> getCurrentMeetings(Club club) {
        LocalDateTime now = LocalDateTime.now();
        return meetingRepository.findByClubAndStartTimeBeforeAndEndTimeAfter(club, now, now);
    }

    /**
     * Retrieves meetings scheduled within a specific date range for a club.
     *
     * @param club the club to query
     * @param startDate the beginning of the date range (inclusive)
     * @param endDate the end of the date range (inclusive)
     * @return list of meetings in the range, empty if none
     * @throws MalformedDTOException if endDate is before startDate
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsInDateRange(Club club, LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate)) {
            throw new MalformedDTOException("End date cannot be before start date");
        }
        return meetingRepository.findByClubAndStartTimeBetween(club, startDate, endDate);
    }

    /**
     * Retrieves the next upcoming meeting for a club.
     *
     * @param club the club to query
     * @return the next meeting, or empty if no upcoming meetings exist
     */
    @Transactional(readOnly = true)
    public java.util.Optional<Meeting> getNextMeeting(Club club) {
        List<Meeting> upcoming = getUpcomingMeetings(club);
        return upcoming.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(upcoming.get(0));
    }

    /**
     * Retrieves meetings by location (for in-person meetings).
     *
     * @param location the physical location to search for
     * @return list of meetings at this location, empty if none
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByLocation(String location) {
        return meetingRepository.findByLocationContainingIgnoreCase(location);
    }

    /**
     * Checks if a club has any meetings scheduled.
     *
     * @param club the club to check
     * @return true if at least one meeting exists
     */
    @Transactional(readOnly = true)
    public boolean hasAnyMeetings(Club club) {
        return meetingRepository.existsByClub(club);
    }

    /**
     * Counts total meetings for a club.
     *
     * @param club the club to count meetings for
     * @return total number of meetings
     */
    @Transactional(readOnly = true)
    public long countMeetingsForClub(Club club) {
        return meetingRepository.countByClub(club);
    }

    /**
     * Counts upcoming meetings for a club.
     *
     * @param club the club to count upcoming meetings for
     * @return number of upcoming meetings
     */
    @Transactional(readOnly = true)
    public long countUpcomingMeetings(Club club) {
        return meetingRepository.countByClubAndStartTimeAfter(club, LocalDateTime.now());
    }

    // ====== UPDATE ======

    /**
     * Updates meeting details.
     *
     * <p>Only non-null parameters are updated. Club and creator cannot be changed
     * after creation. If updating times, end time must remain after start time.</p>
     *
     * @param meetingID the ID of the meeting to update
     * @param title new title (optional)
     * @param startTime new start time (optional)
     * @param endTime new end time (optional)
     * @param location new location (optional)
     * @param link new virtual link (optional)
     * @return the updated {@link Meeting}
     * @throws EntityNotFoundException if no meeting with the given ID exists
     * @throws MalformedDTOException if updated times are invalid
     */
    @Transactional
    public Meeting updateMeeting(Long meetingID, String title,
                                 LocalDateTime startTime, LocalDateTime endTime,
                                 String location, String link) {
        Meeting meeting = requireById(meetingID);

        LocalDateTime newStart = startTime != null ? startTime : meeting.getStartTime();
        LocalDateTime newEnd = endTime != null ? endTime : meeting.getEndTime();
        validateMeetingTimes(newStart, newEnd);

        if (title != null && !title.isBlank()) meeting.setTitle(title);
        if (startTime != null) meeting.setStartTime(startTime);
        if (endTime != null) meeting.setEndTime(endTime);
        if (location != null) meeting.setLocation(location);
        if (link != null) meeting.setLink(link);

        return meetingRepository.save(meeting);
    }

    /**
     * Updates meeting details.
     *
     * <p>Trusts the caller to have created a valid {@code Meeting} object</p>
     *
     * @param meeting
     * @return {@code Meeting}
     */
    @Transactional
    public Meeting updateMeeting(Meeting meeting) {
        return meetingRepository.save(meeting);
    }

    /**
     * Reschedules a meeting to new start and end times.
     *
     * @param meetingID the ID of the meeting to reschedule
     * @param newStartTime the new start time
     * @param newEndTime the new end time
     * @return the updated {@link Meeting}
     * @throws EntityNotFoundException if no meeting with the given ID exists
     * @throws MalformedDTOException if new times are invalid
     */
    @Transactional
    public Meeting rescheduleMeeting(Long meetingID, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        validateMeetingTimes(newStartTime, newEndTime);
        Meeting meeting = requireById(meetingID);
        meeting.setStartTime(newStartTime);
        meeting.setEndTime(newEndTime);
        return meetingRepository.save(meeting);
    }

    // ====== DELETE ======

    /**
     * Deletes a meeting by its ID.
     *
     * <p><strong>Cascading Behavior:</strong> This method also deletes all associated
     * attendee records and register entries to maintain data integrity.</p>
     *
     * @param meetingID the ID of the meeting to delete
     * @throws EntityNotFoundException if no meeting with the given ID exists
     */
    @Transactional
    public void deleteMeeting(Long meetingID) {
        Meeting meeting = requireById(meetingID);

        List<MeetingAttendee> attendees = meetingAttendeeService.findAllMeetingAttendances(meeting);
        for (MeetingAttendee attendee : attendees) {
            meetingAttendeeService.deleteMeetingAttendee(meeting, attendee.getUser());
        }

        meetingRepository.delete(meeting);
    }

    /**
     * Deletes all meetings for a specific club.
     *
     * <p>This is typically used when a club is being deleted. All associated
     * attendee and register records are also cleaned up.</p>
     *
     * @param club the club whose meetings should be deleted
     */
    @Transactional
    public void deleteAllMeetingsForClub(Club club) {
        List<Meeting> meetings = getMeetingsForClub(club);
        for (Meeting meeting : meetings) {
            deleteMeeting(meeting.getMeetingID());
        }
    }

    /**
     * Deletes all past meetings for a club.
     *
     * <p>Useful for archival purposes or database cleanup. Only meetings that have
     * already ended are deleted.</p>
     *
     * @param club the club whose past meetings should be deleted
     */
    @Transactional
    public void deletePastMeetings(Club club) {
        List<Meeting> pastMeetings = getPastMeetings(club);
        for (Meeting meeting : pastMeetings) {
            deleteMeeting(meeting.getMeetingID());
        }
    }

    // ====== ATTENDEE MANAGEMENT ======

    /**
     * Registers a user's RSVP for a meeting.
     *
     * <p>This method delegates to {@link MeetingAttendeeService} to create an
     * attendee record with the specified RSVP status.</p>
     *
     * @param meeting the meeting to RSVP for
     * @param user the user RSVPing
     * @param rsvpStatus the RSVP status (attending, maybe, pass, etc.)
     * @return the created {@link MeetingAttendee} record
     * @throws jakarta.persistence.EntityExistsException if user already has an RSVP
     */
    @Transactional
    public MeetingAttendee registerAttendee(Meeting meeting, User user, RsvpStatus rsvpStatus) {
        return meetingAttendeeService.createMeetingAttendee(user, meeting, rsvpStatus);
    }

    /**
     * Updates a user's RSVP status for a meeting.
     *
     * @param meeting the meeting
     * @param user the user updating their RSVP
     * @param newStatus the new RSVP status
     * @return the updated {@link MeetingAttendee} record
     * @throws EntityNotFoundException if no RSVP record exists
     */
    @Transactional
    public MeetingAttendee updateAttendeeStatus(Meeting meeting, User user, RsvpStatus newStatus) {
        return meetingAttendeeService.updateStatus(meeting, user, newStatus);
    }

    /**
     * Removes a user's RSVP from a meeting.
     *
     * @param meeting the meeting
     * @param user the user canceling their RSVP
     */
    @Transactional
    public void removeAttendee(Meeting meeting, User user) {
        meetingAttendeeService.deleteMeetingAttendee(meeting, user);
    }

    /**
     * Retrieves all users who have RSVPed to a meeting.
     *
     * @param meeting the meeting to query
     * @return list of all users with RSVP records, regardless of status
     */
    @Transactional(readOnly = true)
    public List<User> getAttendeesForMeeting(Meeting meeting) {
        return meetingAttendeeService.findAllMeetingAttendances(meeting)
                .stream()
                .map(MeetingAttendee::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves users who RSVPed with a specific status.
     *
     * @param meeting the meeting to query
     * @param status the RSVP status to filter by
     * @return list of users with matching RSVP status
     */
    @Transactional(readOnly = true)
    public List<User> getAttendeesByStatus(Meeting meeting, RsvpStatus status) {
        return meetingAttendeeService.findAllMeetingAttendances(meeting, status)
                .stream()
                .map(MeetingAttendee::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Counts total RSVPs for a meeting.
     *
     * @param meeting the meeting to count RSVPs for
     * @return total number of RSVP records
     */
    @Transactional(readOnly = true)
    public int getAttendeeCount(Meeting meeting) {
        return meetingAttendeeService.findAllMeetingAttendances(meeting).size();
    }

    /**
     * Counts RSVPs with a specific status.
     *
     * @param meeting the meeting to query
     * @param status the RSVP status to count
     * @return number of RSVPs with the specified status
     */
    @Transactional(readOnly = true)
    public int getAttendeeCountByStatus(Meeting meeting, RsvpStatus status) {
        return meetingAttendeeService.findAllMeetingAttendances(meeting, status).size();
    }

    /**
     * Gets a breakdown of RSVP statuses for a meeting.
     *
     * @param meeting the meeting to analyze
     * @return map of RSVP status to count
     */
    @Transactional(readOnly = true)
    public Map<RsvpStatus, Long> getAttendeeStatusBreakdown(Meeting meeting) {
        return meetingAttendeeService.findAllMeetingAttendances(meeting)
                .stream()
                .collect(Collectors.groupingBy(
                        attendee -> RsvpStatus.valueOf(attendee.getRsvpStatus().toString()),
                        Collectors.counting()
                ));
    }

    /**
     * Checks if a user has RSVPed to a meeting.
     *
     * @param meeting the meeting to check
     * @param user the user to check
     * @return true if an RSVP record exists
     */
    @Transactional(readOnly = true)
    public boolean hasUserRSVPed(Meeting meeting, User user) {
        try {
            meetingAttendeeService.getMeetingAttendance(meeting, user);
            return true;
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    /**
     * Retrieves all meetings a user has RSVPed to.
     *
     * @param user the user to query
     * @return list of meetings the user has RSVPed to
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsForUser(User user) {
        return meetingAttendeeService.findAllMeetingAttendances(user)
                .stream()
                .map(MeetingAttendee::getMeeting)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all meetings a user has RSVPed to with a specific status.
     *
     * @param user the user to query
     * @param status the RSVP status to filter by
     * @return list of meetings matching the criteria
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsForUserByStatus(User user, RsvpStatus status) {
        return meetingAttendeeService.findAllMeetingAttendances(user, status)
                .stream()
                .map(MeetingAttendee::getMeeting)
                .collect(Collectors.toList());
    }

    // ====== VALIDATION HELPERS ======

    /**
     * Validates that meeting times are logical.
     *
     * @param startTime the proposed start time
     * @param endTime the proposed end time
     * @throws MalformedDTOException if times are null, equal, or end is before start
     */
    private void validateMeetingTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new MalformedDTOException("Start time and end time cannot be null");
        }
        if (endTime.isBefore(startTime)) {
            throw new MalformedDTOException("End time cannot be before start time");
        }
        if (endTime.isEqual(startTime)) {
            throw new MalformedDTOException("End time cannot be the same as start time");
        }
    }
}
