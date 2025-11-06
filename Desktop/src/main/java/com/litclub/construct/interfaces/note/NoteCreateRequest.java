package com.litclub.construct.interfaces.note;

public record NoteCreateRequest(
        Long bookID,
        Long clubID,
        String content,
        boolean isPrivate
) {
}
