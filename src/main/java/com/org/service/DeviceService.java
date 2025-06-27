package com.org.service;

import com.org.model.Device;
import com.org.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    public Device save(Device device) {
        device.setLastUpdated(java.time.LocalDateTime.now());
        return deviceRepository.save(device);
    }

    public Optional<Device> getByDeviceId(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    public List<Device> getAll() {
        return deviceRepository.findAll();
    }

    public boolean exists(String deviceId) {
        return deviceRepository.existsByDeviceId(deviceId);
    }

    public void delete(ObjectId id) {
        deviceRepository.deleteById(id);
    }

    public void deviceStatusUpdate(String deviceId, boolean available) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setAvailable(available); // ✅ use correct boolean setter
            device.setLastUpdated(java.time.LocalDateTime.now());
            deviceRepository.save(device);
        } else {
            throw new RuntimeException("Device not found with ID: " + deviceId);
        }
    }


    // ✅ Cleanly named admin method to set device status (AVAILABLE / UNAVAILABLE)
    public void updateAvailability(String deviceId, boolean isAvailable) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setAvailable(isAvailable);  // boolean setter
            device.setLastUpdated(LocalDateTime.now());
            deviceRepository.save(device);
        } else {
            throw new RuntimeException("Device not found with ID: " + deviceId);
        }
    }


}
