package com.litclub.Backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "ok",
                        "serverType", "LitClub",
                        "version", "0.1.0"
                )
        );
    }

}
