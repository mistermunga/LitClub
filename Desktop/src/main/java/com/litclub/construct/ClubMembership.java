package com.litclub.construct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.litclub.construct.enums.ClubRole;
import com.litclub.construct.compositeKey.ClubMembershipID;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClubMembership {

    private ClubMembershipID clubMembershipID;
    private User member;
    private Club club;
    private Set<ClubRole> roles = new HashSet<>();
    private LocalDateTime joinedAt;

    public ClubMembershipID getClubMembershipID() {
        return clubMembershipID;
    }

    public void setClubMembershipID(ClubMembershipID clubMembershipID) {
        this.clubMembershipID = clubMembershipID;
    }

    public User getMember() {
        return member;
    }

    public void setMember(User member) {
        this.member = member;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public Set<ClubRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<ClubRole> roles) {
        this.roles = roles;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}

