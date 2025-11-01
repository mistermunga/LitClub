package com.litclub.Backend.construct.library;

import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.Review;

import java.util.List;

public record UserLibrary(
        UserRecord user,
        List<BookWithStatus> currentlyReading,
        List<BookWithStatus> wantToRead,
        List<BookWithStatus> read,
        List<BookWithStatus> dnf,
        List<Review> recentReviews
) {}

