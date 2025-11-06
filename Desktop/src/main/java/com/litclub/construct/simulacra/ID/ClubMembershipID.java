package com.litclub.construct.simulacra.ID;

public class ClubMembershipID {
    private Long clubID;
    private Long memberID;

    public ClubMembershipID() {}

    public ClubMembershipID(Long clubID, Long memberID) {
        this.clubID = clubID;
        this.memberID = memberID;
    }

    public Long getClubID() {
        return clubID;
    }

    public void setClubID(Long clubID) {
        this.clubID = clubID;
    }

    public Long getMemberID() {
        return memberID;
    }

    public void setMemberID(Long memberID) {
        this.memberID = memberID;
    }
}
