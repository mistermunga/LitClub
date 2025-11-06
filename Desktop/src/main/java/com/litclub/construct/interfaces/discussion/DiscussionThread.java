package com.litclub.construct.interfaces.discussion;

import com.litclub.construct.DiscussionPrompt;
import com.litclub.construct.interfaces.note.NoteWithReplies;

import java.util.List;

public record DiscussionThread(
        DiscussionPrompt prompt,
        List<NoteWithReplies> notes,
        int totalNotes,
        int totalReplies
) {
}
