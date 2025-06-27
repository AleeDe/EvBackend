package com.org.mqtt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.model.Telemetry;
import com.org.service.NotificationService;
import com.org.service.TelemetryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MqttTelemetryListener {

    @Autowired
    private TelemetryService telemetryService;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;


    private static final String broker = "ssl://6329c7604c7c4562b98c235d6a4391aa.s1.eu.hivemq.cloud:8883";
    private static final String clientId = "SpringBootClient123";
    private static final String topic = "ev/telemetry/#";
    private static final String username = "muhammadali";
    private static final String password = "Alialiali110@";

    @PostConstruct
    public void init() {
        try {
            MqttClient client = new MqttClient(broker, clientId, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());

            client.connect(options);
            System.out.println("Connected to HiveMQ Cloud");

            client.subscribe(topic, (t, msg) -> {
                String payload = new String(msg.getPayload(), StandardCharsets.UTF_8);
                System.out.println("Incoming telemetry: " + payload);

                try {
                    Telemetry incoming = objectMapper.readValue(payload, Telemetry.class);
                    incoming.setTimestamp(LocalDateTime.now());
                    telemetryService.save(incoming);
                } catch (Exception e) {
                    System.out.println("Failed to parse/save telemetry: " + e.getMessage());
                }
            });

        } catch (MqttException e) {
            System.out.println("MQTT connection error: " + e.getMessage());
        }
    }
}
