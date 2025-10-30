package com.litclub.Backend.construct.user;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Club;

import java.util.List;

public record UserProfile(
        UserRecord user,
        List<Club> clubs,
        List<Book> books,
        int totalMeetingsAttended,
        int totalReviews,
        int totalNotes
) {}
