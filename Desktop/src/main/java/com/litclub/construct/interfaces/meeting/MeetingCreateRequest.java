package com.litclub.construct.interfaces.meeting;

import java.time.LocalDateTime;

public record MeetingCreateRequest(
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        String link
) {}

