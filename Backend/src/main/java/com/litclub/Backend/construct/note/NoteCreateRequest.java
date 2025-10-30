package com.litclub.Backend.construct.note;

public record NoteCreateRequest(
        Long bookID,
        Long clubID,
        String content,
        boolean isPrivate
) {
}
