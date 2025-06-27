package com.org.service;

import com.org.model.Notification;
import com.org.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;

    public void createNotification(String deviceId, ObjectId userId, String message, String type) {
        Notification notification = Notification.builder()
                .deviceId(deviceId)
                .userId(userId)
                .message(message)
                .type(type)
                .timestamp(LocalDateTime.now())
                .seen(false)
                .build();
        notificationRepo.save(notification);
    }

    public List<Notification> getUserNotifications(ObjectId userId) {
        return notificationRepo.findByUserId(userId);
    }

    public List<Notification> getUnread(ObjectId userId) {
        return notificationRepo.findByUserIdAndSeenFalse(userId);
    }

    public void markAllAsSeen(ObjectId userId) {
        List<Notification> list = getUnread(userId);
        for (Notification n : list) {
            n.setSeen(true);
        }
        notificationRepo.saveAll(list);
    }
}
