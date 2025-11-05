package com.litclub.construct;

import java.time.LocalDateTime;
import java.util.Optional;

public record MeetingRecord(
        String meetingName,
        LocalDateTime start,
        LocalDateTime end,
        ClubRecord club,
        String location,
        Optional<String> link
) {
}
