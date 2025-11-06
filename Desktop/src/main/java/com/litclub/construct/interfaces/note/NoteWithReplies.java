package com.litclub.construct.interfaces.note;

import com.litclub.construct.Note;
import com.litclub.construct.Reply;

import java.util.List;

public record NoteWithReplies(
        Note note,
        List<Reply> replies,
        int replyCount
) {
}
