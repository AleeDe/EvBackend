package com.org.repository;

import com.org.model.Notification;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, ObjectId> {
    List<Notification> findByUserId(ObjectId userId);
    List<Notification> findByUserIdAndSeenFalse(ObjectId userId);
}
