package com.litclub.Backend.construct.meeting;

import com.litclub.Backend.config.ConfigurationManager;

import java.time.LocalDateTime;

public record MeetingCreateRequest(
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        String link
) {}

