package com.litclub.Backend.construct.user;

import com.litclub.Backend.construct.library.book.BookDTO;
import com.litclub.Backend.entity.Club;

import java.util.List;

public record UserProfile(
        UserRecord user,
        List<Club> clubs,
        List<BookDTO> books,
        int totalMeetingsAttended,
        int totalReviews,
        int totalNotes
) {}
