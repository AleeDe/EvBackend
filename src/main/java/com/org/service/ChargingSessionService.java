package com.org.service;

import com.org.repository.UserRepository;
import com.org.model.User;
import com.org.model.ChargingSession;
import com.org.repository.ChargingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.org.model.Telemetry;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ChargingSessionService {

    @Autowired
    private ChargingSessionRepository sessionRepository;
    @Autowired
    private TelemetryService telemetryService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    // ✅ Start session by extracting email (from JWT)
    public ChargingSession startSession(String deviceId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ObjectId userId = user.getId();

        // Check if device already has an active session
        ChargingSession latest = sessionRepository.findTopByDeviceIdOrderByStartTimeDesc(deviceId);
        if (latest != null && latest.getEndTime() == null) {
            throw new RuntimeException("Charging station is currently busy. Please try later.");
        }

        Telemetry telemetry = telemetryService.getLatestByDevice(deviceId);
        if (telemetry == null) throw new RuntimeException("No telemetry found for device");

        ChargingSession session = ChargingSession.builder()
                .deviceId(deviceId)
                .userId(userId)
                .startTime(LocalDateTime.now())
                .status("Started")
                .build();

        ChargingSession saved = sessionRepository.save(session);

        notificationService.createNotification(
                deviceId,
                userId,
                "🔌 Charging started at " + saved.getStartTime().toLocalTime(),
                "INFO"
        );

        return saved;
    }

    // ✅ Stop session and calculate everything

    public ChargingSession stopSession(String deviceId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ObjectId userId = user.getId();

        // Get latest session for the device
        ChargingSession session = sessionRepository.findTopByDeviceIdOrderByStartTimeDesc(deviceId);
        if (session == null || session.getEndTime() != null) {
            throw new RuntimeException("No active session to stop for this device.");
        }

        // Only the user who started the session can stop it
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("You are not authorized to stop this session.");
        }

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(session.getStartTime(), endTime);

        Telemetry latest = telemetryService.getLatestByDevice(deviceId);
        if (latest == null) throw new RuntimeException("No telemetry found");

        double voltage = latest.getVoltage();
        double current = latest.getCurrent();
        double hours = duration.toMinutes() / 60.0;
        double energy = voltage * current * hours / 1000.0;
        double cost = energy * 1500.0; // PKR/kWh

        session.setEndTime(endTime);
        session.setDuration(duration);
        session.setEnergyConsumed(energy);
        session.setCost(cost);
        session.setStatus("Completed");

        ChargingSession saved = sessionRepository.save(session);

        notificationService.createNotification(
                saved.getDeviceId(),
                saved.getUserId(),
                "✅ Charging complete. Energy: " + String.format("%.2f", energy) + " kWh, Cost: " + (int) cost + " PKR",
                "INFO"
        );

        return saved;
    }


    // ✅ Get all sessions by a specific user
    public List<ChargingSession> getSessionsByUser(ObjectId userId) {
        return sessionRepository.findByUserId(userId);
    }

    public List<ChargingSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    // ✅ Get sessions for a user on a specific date (e.g. today)
    public List<ChargingSession> getSessionsByUserAndDate(ObjectId userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return sessionRepository.findByUserIdAndStartTimeBetween(userId, start, end);
    }

    // ✅ Get latest session for a user and device (for UI / stop logic)
    public ChargingSession getLatestByUserAndDevice(ObjectId userId, String deviceId) {
        return sessionRepository.findTopByUserIdAndDeviceIdOrderByStartTimeDesc(userId, deviceId);
    }
    public ChargingSession findById(ObjectId id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    public ChargingSession getLatestByDevice(String deviceId) {
        return sessionRepository.findTopByDeviceIdOrderByStartTimeDesc(deviceId);
    }

    public Double getTotalEnergyConsumedByUser(ObjectId userId) {
        List<ChargingSession> sessions = sessionRepository.findByUserId(userId);
        return sessions.stream()
                .mapToDouble(ChargingSession::getEnergyConsumed)
                .sum();
    }

    public Double getTotalCostByUser(ObjectId userId) {
        List<ChargingSession> sessions = sessionRepository.findByUserId(userId);
        return sessions.stream()
                .mapToDouble(ChargingSession::getCost)
                .sum();
    }

    public int getTotalSessionsByUser(ObjectId userId) {
        return sessionRepository.countByUserId(userId);
    }

    public Duration getTotalChargingTimeByUser(ObjectId userId) {
        List<ChargingSession> sessions = sessionRepository.findByUserId(userId);
        return sessions.stream()
                .map(ChargingSession::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

}
