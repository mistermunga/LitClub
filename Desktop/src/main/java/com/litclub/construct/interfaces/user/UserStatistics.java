package com.litclub.construct.interfaces.user;

public record UserStatistics(
        Long userId,
        int clubsJoined,
        int booksRead,
        int booksReading,
        int booksDropped,
        int meetingsAttended,
        int meetingsUpcoming,
        int reviewsWritten,
        int notesCreated,
        int promptsPosted,
        double averageRating
) {}
