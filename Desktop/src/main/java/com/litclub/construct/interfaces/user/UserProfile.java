package com.litclub.construct.interfaces.user;

import com.litclub.construct.Club;
import com.litclub.construct.interfaces.library.book.BookDTO;

import java.util.List;

public record UserProfile(
        UserRecord user,
        List<Club> clubs,
        List<BookDTO> books,
        int totalMeetingsAttended,
        int totalReviews,
        int totalNotes
) {}
