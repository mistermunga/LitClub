package com.litclub.Backend.service.low;

import com.litclub.Backend.construct.book.BookStatus;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.entity.MeetingAttendee;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.repository.MeetingAttendeeRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MeetingAttendeeService {

    private final MeetingAttendeeRepository meetingAttendeeRepository;

    public MeetingAttendeeService(MeetingAttendeeRepository meetingAttendeeRepository) {
        this.meetingAttendeeRepository = meetingAttendeeRepository;
    }

    // ====== CREATE ======
    @Transactional
    public MeetingAttendee createMeetingAttendee(User user, Meeting meeting, BookStatus bookStatus) {
        if (meetingAttendeeRepository.existsByMeetingAndUser(meeting, user)) {
            throw new EntityExistsException("User already enrolled to meetings");
        }

        MeetingAttendee meetingAttendee = new MeetingAttendee();
        meetingAttendee.setUser(user);
        meetingAttendee.setMeeting(meeting);
        meetingAttendee.setRsvpStatus(bookStatus.toString());

        return meetingAttendeeRepository.save(meetingAttendee);
    }

    // ====== READ ======
    @Transactional(readOnly = true)
    public List<MeetingAttendee> getAllMeetingAttendees() {
        return meetingAttendeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MeetingAttendee getMeetingAttendance(Meeting meeting, User user) {
        return meetingAttendeeRepository.findByMeetingAndUser(meeting, user)
                .orElseThrow(() -> new EntityNotFoundException("User not registered to meeting"));
    }

    @Transactional(readOnly = true)
    public List<MeetingAttendee> findAllMeetingAttendances(Meeting meeting) {
        return meetingAttendeeRepository.findByMeeting(meeting);
    }

    @Transactional(readOnly = true)
    public List<MeetingAttendee> findAllMeetingAttendances(Club club) {
        return meetingAttendeeRepository.findByMeeting_Club(club);
    }

    @Transactional(readOnly = true)
    public List<MeetingAttendee> findAllMeetingAttendances(User user) {
        return meetingAttendeeRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<MeetingAttendee> findAllMeetingAttendances(BookStatus bookStatus) {
        return meetingAttendeeRepository.findByRsvpStatus(bookStatus.toString());
    }

    @Transactional(readOnly = true)
    public List<MeetingAttendee> findAllMeetingAttendances(Meeting meeting, BookStatus bookStatus) {
        return meetingAttendeeRepository.findByMeetingAndRsvpStatus(meeting, bookStatus.toString());
    }

    @Transactional(readOnly = true)
    public List<MeetingAttendee> findAllMeetingAttendances(Club club, BookStatus bookStatus) {
        return meetingAttendeeRepository.findByMeeting_ClubAndRsvpStatus(club, bookStatus.toString());
    }

    @Transactional(readOnly = true)
    public List<MeetingAttendee> findAllMeetingAttendances(User user, BookStatus status) {
        return meetingAttendeeRepository.findByUserAndRsvpStatus(user, status.toString());
    }

    // ====== UPDATE ======
    @Transactional
    public MeetingAttendee updateStatus(Meeting meeting, User user, BookStatus status) {
        MeetingAttendee meetingAttendance = getMeetingAttendance(meeting, user);
        meetingAttendance.setRsvpStatus(status.toString());
        return meetingAttendeeRepository.save(meetingAttendance);
    }

    // ====== DELETE ======
    @Transactional
    public void deleteMeetingAttendee(Meeting meeting, User user) {
        MeetingAttendee meetingAttendance = getMeetingAttendance(meeting, user);
        meetingAttendeeRepository.delete(meetingAttendance);
    }

    @Transactional
    public void purgeClubAttendance(Club club) {
        List<MeetingAttendee> attendees = findAllMeetingAttendances(club);
        meetingAttendeeRepository.deleteAll(attendees);
    }
}
