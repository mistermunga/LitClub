package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.entity.MeetingRegister;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.entity.compositeKey.RegisterID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingRegisterRepository extends JpaRepository<MeetingRegister, RegisterID> {

    boolean existsByMeetingAndUser(Meeting meeting, User user);

    Optional<MeetingRegister> findByMeetingAndUser(Meeting meeting, User user);

    List<MeetingRegister> findAllByUser(User user);
    List<MeetingRegister> findAllByMeeting(Meeting meeting);

    List<MeetingRegister> findAllByAttended(boolean attended);
    List<MeetingRegister> findAllByLate(boolean late);
    List<MeetingRegister> findAllByExcused(boolean excused);
    List<MeetingRegister> findAllByAttendedAndLate(boolean attended, boolean late);
    List<MeetingRegister> findAllByAttendedAndExcused(boolean attended, boolean excused);

    List<MeetingRegister> findAllByUserAndAttended(User user, boolean attended);
    List<MeetingRegister> findAllByUserAndLate(User user, boolean late);
    List<MeetingRegister> findAllByUserAndAttendedAndExcused(User user, boolean attended, boolean excused);
    List<MeetingRegister> findAllByUserAndAttendedAndLate(User user, boolean attended, boolean late);

    List<MeetingRegister> findAllByMeetingAndAttended(Meeting meeting, boolean attended);
    List<MeetingRegister> findAllByMeetingAndLate(Meeting meeting, boolean late);
    List<MeetingRegister> findAllByMeetingAndAttendedAndLate(Meeting meeting, boolean attended, boolean late);
    List<MeetingRegister> findAllByMeetingAndAttendedAndExcused(Meeting meeting, boolean attended, boolean excused);

}
