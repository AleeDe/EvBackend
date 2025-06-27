package com.org.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Telemetry {
    @Id
    private ObjectId id;
    private String deviceId;
    private double voltage;
    private double current;
    private double temperature;
    private LocalDateTime timestamp;
}

