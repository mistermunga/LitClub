package com.litclub.Backend.construct.library;

public record ReviewRequest(
        int rating,
        String content
) {}
