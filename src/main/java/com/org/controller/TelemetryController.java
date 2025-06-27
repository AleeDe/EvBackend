package com.org.controller;

import com.org.model.Telemetry;
import com.org.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @GetMapping("/{deviceId}/latest")
    public ResponseEntity<Telemetry> getLatestByDevice(@PathVariable String deviceId) {
        Telemetry latest = telemetryService.getLatestByDevice(deviceId);
        return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{deviceId}/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Telemetry>> getAllByDevice(@PathVariable String deviceId) {
        return ResponseEntity.ok(telemetryService.getAllByDevice(deviceId));
    }
}
