package com.litclub.Backend.security.userdetails;


import com.litclub.Backend.entity.User;
import com.litclub.Backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsernameWithMembershipsAndRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + username));
        return new CustomUserDetails(user);
    }
}

