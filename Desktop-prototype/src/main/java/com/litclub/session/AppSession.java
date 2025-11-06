package com.litclub.session;

import com.litclub.construct.ClubRecord;
import com.litclub.construct.record.user.UserRecord;
import com.litclub.construct.simulacra.Club;

import java.net.URL;
import java.util.Set;

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
            String email,
            Set<Club> clubs
    ) {
        this.userRecord = new UserRecord((long) id, firstname, lastname, username, email, clubs);
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
