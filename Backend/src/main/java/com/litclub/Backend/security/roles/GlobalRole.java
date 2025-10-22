package com.litclub.Backend.security.roles;

import org.springframework.security.core.GrantedAuthority;

public enum GlobalRole implements GrantedAuthority {

    ADMINISTRATOR, USER;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
