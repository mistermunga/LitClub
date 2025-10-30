package com.litclub.Backend.construct.meeting;

import java.time.LocalDateTime;

public record MeetingUpdateRequest(
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        String link
) {}
