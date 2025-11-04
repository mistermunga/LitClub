package com.litclub.Backend.controller.admin;

import com.litclub.Backend.config.ConfigurationManager;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.service.top.gatekeeper.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/elevate")
    public ResponseEntity<UserRecord> elevateToAdministrator(
            @RequestBody Long userID
    ) {
        return ResponseEntity.ok(adminService.promoteAdmin(userID));
    }

    @PutMapping("/settings")
    public ResponseEntity<ConfigurationManager.InstanceSettings> updateInstanceSettings(
            @RequestBody ConfigurationManager.InstanceSettings instanceSettings
    ) throws IOException {
        return ResponseEntity.ok(adminService.updateInstanceSettings(instanceSettings));
    }

}
