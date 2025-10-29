package com.litclub.Backend.repository;

import com.litclub.Backend.construct.book.BookStatus;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.entity.MeetingAttendee;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.entity.compositeKey.MeetingAttendeeID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingAttendeeRepository extends JpaRepository<MeetingAttendee, MeetingAttendeeID> {

    boolean existsByMeetingAndUser(Meeting meeting, User user);

    Optional<MeetingAttendee> findByMeetingAndUser(Meeting meeting, User user);

    List<MeetingAttendee> findByMeeting(Meeting meeting);
    List<MeetingAttendee> findByMeeting_Club(Club club);
    List<MeetingAttendee> findByUser(User user);
    List<MeetingAttendee> findByRsvpStatus(String status);

    List<MeetingAttendee> findByMeetingAndRsvpStatus(Meeting meeting, String status);
    List<MeetingAttendee> findByMeeting_ClubAndRsvpStatus(Club club, String status);
    List<MeetingAttendee> findByUserAndRsvpStatus(User user, String status);

}
