package com.litclub.Backend.controller.admin;

import com.litclub.Backend.config.ConfigurationManager;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.security.roles.GlobalRole;
import com.litclub.Backend.security.userdetails.CustomUserDetails;
import com.litclub.Backend.service.top.gatekeeper.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/elevate")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<UserRecord> elevateToAdministrator(
            @RequestBody Long userID
    ) {
        return ResponseEntity.ok(adminService.promoteAdmin(userID));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ConfigurationManager.InstanceSettings> updateInstanceSettings(
            @RequestBody ConfigurationManager.InstanceSettings instanceSettings
    ) throws IOException {
        return ResponseEntity.ok(adminService.updateInstanceSettings(instanceSettings));
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LoadedInstanceSettings> getInstanceSettings(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        return ResponseEntity.ok(new LoadedInstanceSettings(
                adminService.getInstanceSettings(),
                customUserDetails.getAuthorities().contains(GlobalRole.ADMINISTRATOR)
        ));
    }

    public record LoadedInstanceSettings(
            ConfigurationManager.InstanceSettings instanceSettings,
            Boolean isAdmin
    ) {}
}
