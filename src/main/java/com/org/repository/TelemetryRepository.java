package com.org.repository;

import com.org.model.Telemetry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TelemetryRepository extends MongoRepository<Telemetry, ObjectId> {
    List<Telemetry> findByDeviceIdOrderByTimestampDesc(String deviceId);

    Telemetry findFirstByDeviceIdOrderByTimestampDesc(String deviceId);

}
