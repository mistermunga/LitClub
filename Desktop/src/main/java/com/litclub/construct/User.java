package com.litclub.construct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.litclub.construct.enums.GlobalRole;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private Long userID;
    private String username;
    private String firstName;
    private String secondName;
    private String email;
    private Set<GlobalRole> globalRoles = new HashSet<>();
    private LocalDateTime createdAt;

    public User () {}

    public User (
            String username,
            String firstName,
            String surname,
            String email,
            boolean isAdmin
    ) {

        this.username = username;
        this.firstName = firstName;
        this.secondName = surname;
        this.email = email;
        this.globalRoles.add(GlobalRole.USER);
        if (isAdmin) {this.globalRoles.add(GlobalRole.ADMINISTRATOR);}
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<GlobalRole> getGlobalRoles() {
        return globalRoles;
    }

    public void setGlobalRoles(Set<GlobalRole> globalRoles) {
        this.globalRoles = globalRoles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

