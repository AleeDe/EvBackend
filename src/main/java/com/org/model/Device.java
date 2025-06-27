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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Device {
    @Id
    private ObjectId id;
    private String deviceId;
    private String location;
    private String name;
    private boolean available;  // instead of private String status;
    private LocalDateTime lastUpdated;
}
