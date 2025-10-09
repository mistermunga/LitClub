package com.litclub.session;

import com.litclub.session.construct.ClubRecord;
import com.litclub.session.construct.UserRecord;

public class AppSession {

    private static AppSession instance;
    private UserRecord userRecord;
    private ClubRecord clubRecord;
    private boolean brightMode = true;

    private AppSession() {}

    public static AppSession getInstance() {
        if (instance == null) {
            instance = new AppSession();
        }
        return instance;
    }

    public boolean isBrightMode() {
        return brightMode;
    }

    public void setBrightMode(boolean brightMode) {
        this.brightMode = brightMode;
    }

    public void setCredentials(
            String firstname,
            String lastname,
            String username,
            String email
    ) {
        this.userRecord = new UserRecord(firstname, lastname, username, email);
    }

    public void setClubDetails(
            String clubName,
            boolean isAdministrator
    ) {
        this.clubRecord = new ClubRecord(clubName, isAdministrator);
    }
}
