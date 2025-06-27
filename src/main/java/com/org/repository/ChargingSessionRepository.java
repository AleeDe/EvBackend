package com.org.repository;


import com.org.model.ChargingSession;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChargingSessionRepository extends MongoRepository<ChargingSession, ObjectId> {

    List<ChargingSession> findByUserId(ObjectId userId);
    List<ChargingSession> findByUserIdAndStartTimeBetween(ObjectId userId, LocalDateTime from, LocalDateTime to);
    ChargingSession findTopByUserIdAndDeviceIdOrderByStartTimeDesc(ObjectId userId, String deviceId);

    ChargingSession findTopByDeviceIdOrderByStartTimeDesc(String deviceId);
    int countByUserId(ObjectId userId);
}
