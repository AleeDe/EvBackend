package com.org.scheduler;

import com.org.model.ChargingSession;
import com.org.model.Telemetry;
import com.org.service.ChargingSessionService;
import com.org.service.NotificationService;
import com.org.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelemetryAlertScheduler {

    private final ChargingSessionService sessionService;
    private final TelemetryService telemetryService;
    private final NotificationService notificationService;

    private static final String DEVICE_ID = "DEVICE123"; // ✅ your only device

    @Scheduled(fixedRate = 30000) // runs every 30 seconds
    public void monitorOneDevice() {
        // ✅ 1. Get latest session for the device
        ChargingSession session = sessionService.getLatestByDevice(DEVICE_ID);
        if (session == null || session.getEndTime() != null) return; // not charging

        ObjectId userId = session.getUserId(); // who started session

        // ✅ 2. Get latest telemetry
        Telemetry latest = telemetryService.getLatestByDevice(DEVICE_ID);
        if (latest == null) return;

        // ✅ 3. Check and notify
        if (latest.getVoltage() > 250 || latest.getVoltage() < 180) {
            notificationService.createNotification(DEVICE_ID, userId,
                    "⚠️ Voltage Alert: " + latest.getVoltage() + "V", "ALERT");
        }

        if (latest.getCurrent() > 30) {
            notificationService.createNotification(DEVICE_ID, userId,
                    "⚠️ High Current: " + latest.getCurrent() + "A", "ALERT");
        }

        if (latest.getTemperature() > 60) {
            notificationService.createNotification(DEVICE_ID, userId,
                    "🔥 High Temperature: " + latest.getTemperature() + "°C", "ALERT");
        }
    }
}
