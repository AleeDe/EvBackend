package com.org.controller;

import com.org.model.Device;

import com.org.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceService.getAll();
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Device> addDevice(@RequestBody Device device) {
        return ResponseEntity.ok(deviceService.save(device));
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/{deviceId}")
    public ResponseEntity<Device> getDevice(@PathVariable String deviceId) {
        return deviceService.getByDeviceId(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{deviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDeviceStatus(@PathVariable String deviceId, @RequestParam boolean status) {
        try {
            deviceService.deviceStatusUpdate(deviceId, status);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
