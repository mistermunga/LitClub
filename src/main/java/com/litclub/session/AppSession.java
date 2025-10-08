package com.litclub.session;

public class AppSession {

    private static AppSession instance;

    // Hardcoded for prototype
    // TODO: make sessions generic
    private final String username = "john-example";
    private final String fName = "John";
    private final String lName = "Example";
    private boolean brightMode = true;

    private AppSession() {}

    public static AppSession getInstance() {
        if (instance == null) {
            instance = new AppSession();
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public String getfName() {
        return fName;
    }

    public String getlName() {
        return lName;
    }

    public boolean isBrightMode() {
        return brightMode;
    }

    public void setBrightMode(boolean brightMode) {
        this.brightMode = brightMode;
    }
}
