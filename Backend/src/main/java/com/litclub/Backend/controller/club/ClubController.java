package com.litclub.Backend.controller.club;

import com.litclub.Backend.config.ConfigurationManager;
import com.litclub.Backend.construct.club.ClubCreateRequest;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.UserService;
import com.litclub.Backend.service.top.facilitator.ClubActivityService;
import com.litclub.Backend.service.top.gatekeeper.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;
    private final ClubActivityService clubActivityService;
    private final AdminService adminService;
    private final UserService userService;
    private final ConfigurationManager configuration;

    public ClubController(ClubService clubService,
                          ClubActivityService clubActivityService,
                          AdminService adminService,
                          ConfigurationManager configuration,
                          UserService userService) {
        this.clubService = clubService;
        this.clubActivityService = clubActivityService;
        this.adminService = adminService;
        this.userService = userService;
        this.configuration = configuration;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Page<Club>> getClubs(
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getAllClubs(pageable));
    }

    @GetMapping("/{clubID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Club> getClub(
            @PathVariable("clubID") Long clubID
    ) {
        return ResponseEntity.ok(clubService.requireClubById(clubID));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Club> createClub(@Valid @RequestBody ClubCreateRequest clubRequest) {
        // TODO Club queues
        if (configuration
                .getInstanceSettings()
                .clubCreationMode()
                .equals(ConfigurationManager.ClubCreationMode.ADMIN_ONLY)
                &&
                SecurityContextHolder.getContext().getAuthentication()
                        .getAuthorities().stream()
                        .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMINISTRATOR"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User creator = userService.requireUserById(clubRequest.creator().userID());

        Club club = new Club();
        club.setClubName(clubRequest.clubName());
        club.setDescription(clubRequest.description());
        club.setCreator(creator);

        Club registeredClub = clubService.registerClub(club, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredClub);
    }

    @PutMapping("/{clubID}")
    @PreAuthorize("@clubSecurity.isOwner(authentication, clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Club> updateClub(
            @PathVariable("clubID") Long clubID,
            @Valid @RequestBody ClubCreateRequest clubRequest
    ) {
        Club club = clubService.requireClubById(clubID);
        return ResponseEntity.status(HttpStatus.OK).body(clubService.updateClub(clubRequest, club));
    }

    @DeleteMapping("/{clubID}")
    @PreAuthorize("@clubSecurity.isOwner(authentication, clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteClub(
            @PathVariable("clubID") Long clubID
    ) {
        clubService.deleteClub(clubID);
        return ResponseEntity.noContent().build();
    }
}
