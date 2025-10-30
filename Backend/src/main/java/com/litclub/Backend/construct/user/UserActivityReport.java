package com.litclub.Backend.construct.user;

import com.litclub.Backend.entity.*;

import java.util.List;

public record UserActivityReport(
        UserRecord user,
        List<Meeting> upcomingMeetings,
        List<Meeting> pastMeetings,
        List<Review> recentReviews,
        List<Note> recentNotes,
        List<DiscussionPrompt> recentPrompts
) {}

