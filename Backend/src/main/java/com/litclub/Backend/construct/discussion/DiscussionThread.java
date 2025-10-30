package com.litclub.Backend.construct.discussion;

import com.litclub.Backend.construct.note.NoteWithReplies;
import com.litclub.Backend.entity.DiscussionPrompt;

import java.util.List;

public record DiscussionThread(
        DiscussionPrompt prompt,
        List<NoteWithReplies> notes,
        int totalNotes,
        int totalReplies
) {
}
