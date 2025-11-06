package com.litclub.construct;

import com.litclub.persistence.DataRepository;

import java.time.LocalDateTime;

@Deprecated
public class Note {

    private int noteID;
    private int bookID;
    private Integer clubID;
    private int userID;
    private Integer discussionPromptID;
    private String content;
    private boolean isPrivate;
    private LocalDateTime createdAt;

    private transient String bookTitle;
    private transient String authorName;

    public int getNoteID() {
        return noteID;
    }

    public void setNoteID(int noteID) {
        this.noteID = noteID;
    }

    public Integer getDiscussionPromptID() {
        return discussionPromptID;
    }

    public void setDiscussionPromptID(Integer discussionPromptID) {
        this.discussionPromptID = discussionPromptID;
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
        setBookTitle();
        setAuthorName();
    }

    public Integer getClubID() {
        return clubID;
    }

    public void setClubID(Integer clubID) {
        this.clubID = clubID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle() {
        if (this.bookID != 0) {
            this.bookTitle = DataRepository.getInstance()
                    .getBookById(this.bookID)
                    .getTitle();
        }
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName() {
        if (this.bookID != 0) {
            this.authorName = DataRepository.getInstance()
                    .getBookById(this.bookID)
                    .getAuthor();
        }
    }


}
