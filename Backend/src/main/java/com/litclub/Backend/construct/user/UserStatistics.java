package com.litclub.Backend.construct.user;

public record UserStatistics(
        Long userId,
        int clubsJoined,
        int booksRead,
        int booksReading,
        int meetingsAttended,
        int meetingsUpcoming,
        int reviewsWritten,
        int notesCreated,
        int promptsPosted,
        double averageRating
) {}
