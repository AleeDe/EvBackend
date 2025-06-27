package com.org.controller;

import com.org.model.Telemetry;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TelemetryWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishTelemetry(Telemetry telemetry) {
        messagingTemplate.convertAndSend("/topic/telemetry/" + telemetry.getDeviceId(), telemetry);
        System.out.println("Publishing telemetry for device: " + telemetry.getDeviceId());
        System.out.println("Payload: " + telemetry);

    }
}
