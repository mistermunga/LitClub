package com.litclub.construct.interfaces.library;

import com.litclub.construct.Review;
import com.litclub.construct.interfaces.user.UserRecord;

import java.util.List;

public record UserLibrary(
        UserRecord user,
        List<BookWithStatus> currentlyReading,
        List<BookWithStatus> wantToRead,
        List<BookWithStatus> read,
        List<BookWithStatus> dnf,
        List<Review> recentReviews
) {}

