package com.org.controller;

import com.org.model.ChargingSession;
import com.org.model.Device;
import com.org.service.ChargingSessionService;
import com.org.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import com.org.model.User;
import com.org.repository.UserRepository;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ChargingSessionController {

    @Autowired
    private ChargingSessionService sessionService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeviceService deviceService;

    // ✅ Start a new charging session (user authenticated via JWT)
    @PostMapping("/start")
    public ResponseEntity<ChargingSession> startSession(
            @RequestParam String deviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ChargingSession session = sessionService.startSession(deviceId, userDetails.getUsername());
        return ResponseEntity.ok(session);
    }


    // ✅ Stop a charging session
    @PostMapping("/stop")
    public ResponseEntity<ChargingSession> stopSession(
            @RequestParam String deviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ChargingSession session = sessionService.stopSession(deviceId, userDetails.getUsername());
        return ResponseEntity.ok(session);
    }


    // ✅ Get all sessions
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChargingSession>> getAllSessions(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).build(); // Forbidden for non-admin users
        }

        List<ChargingSession> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    // ✅ Get all sessions of current user
    @GetMapping("/user")
    public ResponseEntity<List<ChargingSession>> getUserSessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(sessionService.getSessionsByUser(user.getId()));
    }

    // ✅ Get today's sessions for user
    @GetMapping("/user/today")
    public ResponseEntity<List<ChargingSession>> getUserSessionsToday(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(sessionService.getSessionsByUserAndDate(user.getId(), today));
    }

    // ✅ (Optional) Get latest session by device for logged-in user
    @GetMapping("/latest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChargingSession> getLatestSession(
            @RequestParam String deviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(user.getRole()!= "ADMIN") {
            return ResponseEntity.status(403).build(); // Forbidden for non-admin users
        }
        ChargingSession latest = sessionService.getLatestByUserAndDevice(user.getId(), deviceId);
        return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.notFound().build();
    }

    @GetMapping("/amount")
    public ResponseEntity<Double> getTotalAmount(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        double totalAmount = sessionService.getTotalCostByUser(user.getId());
        return ResponseEntity.ok(totalAmount);
    }

    @GetMapping("/engergy")
    public ResponseEntity<Double> getTotalEnergy(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        double totalEnergy = sessionService.getTotalEnergyConsumedByUser(user.getId());
        return ResponseEntity.ok(totalEnergy);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getTotalSessionsCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int totalSessions = sessionService.getTotalSessionsByUser(user.getId());
        return ResponseEntity.ok(totalSessions);
    }

    // ✅ Check if device is available (no active session)
    @GetMapping("/isInUse")
    public ResponseEntity<Map<String, Object>> isDeviceAvailable(@RequestParam String deviceId) {
        ChargingSession latestSession = sessionService.getLatestByDevice(deviceId);

        boolean isAvailable = (latestSession == null || latestSession.getEndTime() != null);

        return ResponseEntity.ok(Map.of(
                "deviceId", deviceId,
                "isAvailable", isAvailable
        ));
    }

    // ✅ Admin-only: Update the status of a device (AVAILABLE / UNAVAILABLE)
//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/setDeviceAvailability")
    public ResponseEntity<?> setDeviceAvailability(
            @RequestParam String deviceId,
            @RequestParam boolean available) {

        deviceService.updateAvailability(deviceId, available);
        return ResponseEntity.ok(Map.of(
                "deviceId", deviceId,
                "available", available,
                "message", "Device availability updated successfully"
        ));
    }

    // ✅ User endpoint: Check if device is in service (available=true)
    @GetMapping("/isInService")
    public ResponseEntity<Map<String, Object>> isDeviceInService(@RequestParam String deviceId) {
        Optional<Device> deviceOpt = deviceService.getByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "deviceId", deviceId,
                    "error", "Device not found"
            ));
        }

        Device device = deviceOpt.get();

        return ResponseEntity.ok(Map.of(
                "deviceId", deviceId,
                "isAvailable", device.isAvailable()
        ));
    }



}
