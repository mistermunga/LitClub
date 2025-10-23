package com.litclub.Backend.repository;

import com.litclub.Backend.entity.MeetingAttendee;
import com.litclub.Backend.entity.compositeKey.MeetingAttendeeID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingAttendeeRepository extends JpaRepository<MeetingAttendee, MeetingAttendeeID> {
}
