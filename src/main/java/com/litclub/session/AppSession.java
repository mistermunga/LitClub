package com.litclub.session;

public class AppSession {

    private static AppSession instance;

    // TODO: make sessions generic
    private String username = "john-example";
    private String fName = "John";
    private String lName = "Example";
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

    public void setlName(String lName) {
        this.lName = lName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCredentials(String firstname, String lastname, String username) {
        setfName(firstname);
        setlName(lastname);
        setUsername(username);
    }
}
