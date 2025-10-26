package com.litclub.Backend.service;

import com.litclub.Backend.construct.user.UserLoginRecord;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.construct.user.UserRegistrationRecord;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.ClubMembership;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.exception.CredentialsAlreadyTakenException;
import com.litclub.Backend.exception.UserNotFoundException;
import com.litclub.Backend.repository.UserRepository;
import com.litclub.Backend.security.roles.GlobalRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Login
    @Transactional(readOnly = true)
    public UserRecord login(UserLoginRecord userLoginRecord) {
        if (userLoginRecord.username().isPresent()) {
            return loginWithUsername(userLoginRecord);
        } else if (userLoginRecord.email().isPresent()) {
            return loginWithEmail(userLoginRecord);
        } else {
            throw new IllegalArgumentException("Username or Email is required");
        }
    }

    public UserRecord loginWithUsername(UserLoginRecord userLoginRecord) {
        Optional<User> user = userRepository.findUserByUsername(userLoginRecord.username().toString());

        if (user.isEmpty()) {
            throw new BadCredentialsException("Invalid username");
        }

        if (passwordEncoder.matches(userLoginRecord.password(), user.get().getPasswordHash())){
            return convertUserToRecord(user.get());
        }

        throw new BadCredentialsException("Invalid Credentials");

    }

    public UserRecord loginWithEmail(UserLoginRecord userLoginRecord) {
        Optional<User> user = userRepository.findUserByEmail(userLoginRecord.email().toString());

        if (user.isEmpty()) {
            throw new BadCredentialsException("Invalid email");
        }

        if (passwordEncoder.matches(userLoginRecord.password(), user.get().getPasswordHash())){
            return convertUserToRecord(user.get());
        }

        throw new BadCredentialsException("Invalid Credentials");
    }

    // ===== CREATE =====
    @Transactional
    public UserRecord registerUser(UserRegistrationRecord userRegistrationRecord) {
        if (userRepository.existsByUsername(userRegistrationRecord.username())) {
            throw new CredentialsAlreadyTakenException("Username is already in use");
        }

        User user = new User (
                userRegistrationRecord.username(),
                userRegistrationRecord.firstName(),
                userRegistrationRecord.surname(),
                userRegistrationRecord.email(),
                userRegistrationRecord.isAdmin()
        );

        user.setPasswordHash(passwordEncoder.encode(userRegistrationRecord.password()));

        return convertUserToRecord(userRepository.save(user));

    }

    // ===== READ =====
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findUserByUserID(id);
    }

    @Transactional
    public UserRecord updateUser(Long userID, UserRegistrationRecord userRecord) throws UserNotFoundException {
        Optional<User> user = getUserById(userID);

        if (user.isEmpty()) {
            throw new UserNotFoundException("userID", userID.toString());
        }

        User userToUpdate = user.get();

        if (userRecord.username() != null) {userToUpdate.setUsername(userRecord.username());}
        if (userRecord.firstName() != null) {userToUpdate.setFirstName(userRecord.firstName());}
        if (userRecord.surname() != null) {userToUpdate.setSecondName(userRecord.surname());}
        if (userRecord.email() != null) {userToUpdate.setEmail(userRecord.email());}

        if (userRecord.isAdmin()) {userToUpdate.setGlobalRoles(Set.of(GlobalRole.ADMINISTRATOR));}

        if (userRecord.password() != null) {
            userToUpdate.setPasswordHash(passwordEncoder.encode(userRecord.password()));
        }

        userRepository.save(userToUpdate);
        return convertUserToRecord(userToUpdate);
    }

    @Transactional
    public void deleteUser(String identifier) throws UserNotFoundException {
        Optional<User> userOpt = userRepository.findUserByUsername(identifier);

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findUserByEmail(identifier);
        }

        User user = userOpt.orElseThrow(() -> new UserNotFoundException("username or email", identifier));
        userRepository.delete(user);
    }


    // ===== Utility =====
    public UserRecord convertUserToRecord(User user) {
        return new UserRecord(
                user.getUserID(),
                user.getFirstName(),
                user.getSecondName(),
                user.getUsername(),
                user.getEmail(),
                getClubsForUser(user)
        );
    }

    public Set<Club> getClubsForUser(User user) {
        Set<Club> clubs = new HashSet<>();
        Set<ClubMembership> memberships = user.getMemberships();

        for (ClubMembership membership : memberships) {
            clubs.add(membership.getClub());
        }

        return clubs;
    }
}
