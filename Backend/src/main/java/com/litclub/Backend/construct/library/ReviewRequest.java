package com.litclub.Backend.construct.library;

public record ReviewRequest(
        Integer rating,
        String content
) {}
