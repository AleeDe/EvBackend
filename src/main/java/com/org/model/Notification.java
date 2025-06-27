package com.org.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    private ObjectId id;

    private ObjectId userId;           // Who gets the alert (admin or normal)
    private String deviceId;
    private String message;
    private String type;               // ALERT / INFO / PAYMENT
    private boolean seen = false;
    private LocalDateTime timestamp;
}
