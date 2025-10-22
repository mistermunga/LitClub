package com.litclub.session.construct;

public class Reply extends Note{

    private int noteID;

    public Reply() {
        super();
        this.setPrivate(false);
        this.setDiscussionPromptID(null);
    }

    public int getNoteID() {
        return noteID;
    }
    public void setNoteID(int noteID) {
        this.noteID = noteID;
    }
}
