package com.litclub.session;

import com.litclub.construct.Club;
import com.litclub.construct.enums.ClubRole;
import com.litclub.construct.interfaces.user.UserRecord;

import java.net.URL;

public class AppSession {

    private static AppSession instance;
    private static UserRecord userRecord;
    private static Club currentClub;
    private ClubRole highestRole;
    private boolean brightMode = true;
    private URL INSTANCE_URL;
    private boolean isAdmin;

    private AppSession() {}

    public static synchronized AppSession getInstance() {
        if (instance == null) {
            instance = new AppSession();
        }
        return instance;
    }

    public UserRecord getUserRecord() {
        return userRecord;
    }

    public void setUserRecord(UserRecord userRecord) {
        AppSession.userRecord = userRecord;
    }

    public Club getCurrentClub() {return currentClub;}

    public void setCurrentClub(Club currentClub) {AppSession.currentClub = currentClub;}

    public ClubRole getHighestRole() {return highestRole;}

    public void setHighestRole(ClubRole highestRole) {this.highestRole = highestRole;}

    public boolean isBrightMode() {
        return brightMode;
    }

    public void setBrightMode(boolean brightMode) {
        this.brightMode = brightMode;
    }

    public URL getINSTANCE_URL() {
        return INSTANCE_URL;
    }

    public void setINSTANCE_URL(URL INSTANCE_URL) {
        this.INSTANCE_URL = INSTANCE_URL;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void clearClubContext() {
        setHighestRole(null);
        setCurrentClub(null);
    }
}
