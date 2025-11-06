package com.litclub.construct.interfaces.library;

public record ReviewRequest(
        Integer rating,
        String content
) {}
