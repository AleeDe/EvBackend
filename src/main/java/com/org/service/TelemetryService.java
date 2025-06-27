package com.org.service;

import com.org.controller.TelemetryWebSocketPublisher;
import com.org.model.Telemetry;
import com.org.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;
    private final TelemetryWebSocketPublisher telemetryWebSocketPublisher;

    public Telemetry save(Telemetry telemetry) {
        Telemetry saved = telemetryRepository.save(telemetry);

        // After saving, publish to WebSocket
        telemetryWebSocketPublisher.publishTelemetry(saved);

        return saved;
    }

    public List<Telemetry> getAllByDevice(String deviceId) {
        return telemetryRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
    }

    public Telemetry getLatestByDevice(String deviceId) {
        return telemetryRepository.findFirstByDeviceIdOrderByTimestampDesc(deviceId);
    }
}
