package com.litclub.construct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.litclub.construct.compositeKey.ClubBookID;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClubBook {

    private ClubBookID clubBookID;
    private boolean valid;
    private LocalDateTime createdAt;

    public ClubBookID getClubBookID() {
        return clubBookID;
    }

    public void setClubBookID(ClubBookID clubBookID) {
        this.clubBookID = clubBookID;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

