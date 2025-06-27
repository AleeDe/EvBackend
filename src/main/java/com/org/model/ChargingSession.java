package com.org.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class ChargingSession {
    @Id
    private ObjectId id;
    private String deviceId;
    private ObjectId userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration  duration;
    private double energyConsumed;
    private double cost;
    private String status; // Started, Completed, Fault
    private String paymentStatus; // Pending, Paid, Failed

}
