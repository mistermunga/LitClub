package com.litclub.Backend.security.userdetails;

import com.litclub.Backend.entity.User;
import com.litclub.Backend.security.roles.utility.AuthorityMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final User user;
    private final Set<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.user = user;
        this.authorities = AuthorityMapper.mapToAuthorities(user);
    }
    
    public User getUser() {return user;}

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public String getUsername() { return user.getUsername(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

