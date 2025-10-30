package com.litclub.Backend.construct.club;

public record ClubStatistics(
        Long clubId,
        int totalMembers,
        int totalMeetings,
        int totalPrompts,
        int totalNotes,
        int booksRead,
        double averageAttendance
) {}
