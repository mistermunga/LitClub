package com.litclub.construct.interfaces.library;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.litclub.construct.Review;
import com.litclub.construct.interfaces.user.UserRecord;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserLibrary(
        @JsonProperty("user") UserRecord user,
        @JsonProperty("currentlyReading") List<BookWithStatus> currentlyReading,
        @JsonProperty("wantToRead") List<BookWithStatus> wantToRead,
        @JsonProperty("read") List<BookWithStatus> read,
        @JsonProperty("dnf") List<BookWithStatus> dnf,
        @JsonProperty("recentReviews") List<Review> recentReviews
) {}