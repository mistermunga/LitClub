package com.litclub.construct.interfaces.library;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.litclub.construct.Book;
import com.litclub.construct.enums.BookStatus;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookWithStatus(
        @JsonProperty("book") Book book,
        @JsonProperty("status") BookStatus status,
        @JsonProperty("rating") Integer rating,
        @JsonProperty("dateStarted") LocalDate dateStarted,
        @JsonProperty("dateFinished") LocalDate dateFinished
) {}