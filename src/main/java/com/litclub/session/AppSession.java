package com.litclub.session;

import java.util.HashMap;

public class AppSession {

    private static AppSession instance;

    private String username;
    private String fName;
    private String lName;
    private String email;
    private String club;
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

    public String getEmail() { return email; }

    public String getClub() { return club; }

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

    public void setEmail(String email) { this.email = email; }

    public void setClub(String club) { this.club = club; }

    public void setCredentials(String firstname,
                               String lastname,
                               String username,
                               String email) {
        setfName(firstname);
        setlName(lastname);
        setUsername(username);
        setEmail(email);
    }

    public HashMap<String,String> getCredentials() {
        HashMap<String,String> credentials = new HashMap<>();

        credentials.put("firstname", getfName());
        credentials.put("lastname", getlName());
        credentials.put("username", getUsername());
        credentials.put("email", getEmail());

        return credentials;
    }
}
