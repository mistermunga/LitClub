package com.litclub.session;

import com.litclub.construct.interfaces.club.ClubRecord;
import com.litclub.construct.interfaces.user.UserRecord;

import java.net.URL;

public class AppSession {

    private static AppSession instance;
    private static UserRecord userRecord;
    private static ClubRecord clubRecord;
    private boolean brightMode = true;
    private URL INSTANCE_URL;

    private AppSession() {}

    public static AppSession getInstance() {
        if (instance == null) {
            instance = new AppSession();
        }
        return instance;
    }


    public static void setInstance(AppSession instance) {
        AppSession.instance = instance;
    }

    public static UserRecord getUserRecord() {
        return userRecord;
    }

    public static void setUserRecord(UserRecord userRecord) {
        AppSession.userRecord = userRecord;
    }

    public static ClubRecord getClubRecord() {
        return clubRecord;
    }

    public static void setClubRecord(ClubRecord clubRecord) {
        AppSession.clubRecord = clubRecord;
    }

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
}
