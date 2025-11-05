package com.litclub.session;

import com.litclub.session.construct.ClubRecord;
import com.litclub.session.construct.UserRecord;

import java.net.URL;

public class AppSession {

    private static AppSession instance;
    private UserRecord userRecord;
    private ClubRecord clubRecord;
    private boolean brightMode = true;

    private URL INSTANCE_URL;

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
            int id,
            String firstname,
            String lastname,
            String username,
            String email
    ) {
        this.userRecord = new UserRecord(id, firstname, lastname, username, email);
    }

    public void setClubDetails( ClubRecord clubRecord ) {
        this.clubRecord = clubRecord;
    }

    public UserRecord getUserRecord() {
        return userRecord;
    }

    public ClubRecord getClubRecord() {
        return clubRecord;
    }

    public void setINSTANCE_URL(URL INSTANCE_URL) {
        this.INSTANCE_URL = INSTANCE_URL;
    }

    public URL getINSTANCE_URL() {
        return INSTANCE_URL;
    }
}
