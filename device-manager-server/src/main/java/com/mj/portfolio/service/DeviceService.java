package com.mj.portfolio.service;

import com.mj.portfolio.dto.DeviceRequest;
import com.mj.portfolio.dto.DeviceResponse;
import com.mj.portfolio.entity.Device;
import com.mj.portfolio.entity.DeviceLog;
import com.mj.portfolio.entity.enums.DeviceStatus;
import com.mj.portfolio.entity.enums.DeviceType;
import com.mj.portfolio.exception.DeviceNotFoundException;
import com.mj.portfolio.repository.DeviceLogRepository;
import com.mj.portfolio.repository.DeviceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepo;
    private final DeviceLogRepository logRepo;

    public DeviceService(DeviceRepository deviceRepo, DeviceLogRepository logRepo) {
        this.deviceRepo = deviceRepo;
        this.logRepo    = logRepo;
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> findAll(DeviceStatus status, DeviceType type, String q, Pageable pageable) {
        Page<Device> page;
        if (q != null && !q.isBlank()) {
            page = deviceRepo.search(q.trim(), pageable);
        } else if (status != null && type != null) {
            page = deviceRepo.findByStatusAndType(status, type, pageable);
        } else if (status != null) {
            page = deviceRepo.findByStatus(status, pageable);
        } else if (type != null) {
            page = deviceRepo.findByType(type, pageable);
        } else {
            page = deviceRepo.findAll(pageable);
        }
        return page.map(DeviceResponse::from);
    }

    @Transactional(readOnly = true)
    public DeviceResponse findById(UUID id) {
        return DeviceResponse.from(getOrThrow(id));
    }

    public DeviceResponse create(DeviceRequest req) {
        Device device = new Device();
        applyRequest(device, req);
        Device saved = deviceRepo.save(device);
        logRepo.save(new DeviceLog(saved, "CREATED",
                "Device '" + saved.getName() + "' created"));
        return DeviceResponse.from(saved);
    }

    public DeviceResponse update(UUID id, DeviceRequest req) {
        Device device = getOrThrow(id);
        DeviceStatus oldStatus = device.getStatus();
        applyRequest(device, req);
        Device saved = deviceRepo.save(device);
        String desc = "Device '" + saved.getName() + "' updated";
        logRepo.save(new DeviceLog(saved, "UPDATED", desc));
        if (oldStatus != saved.getStatus()) {
            logRepo.save(new DeviceLog(saved, "STATUS_CHANGED",
                    "Status changed from " + oldStatus + " to " + saved.getStatus()));
        }
        return DeviceResponse.from(saved);
    }

    public DeviceResponse updateStatus(UUID id, DeviceStatus newStatus) {
        Device device = getOrThrow(id);
        DeviceStatus oldStatus = device.getStatus();
        device.setStatus(newStatus);
        Device saved = deviceRepo.save(device);
        logRepo.save(new DeviceLog(saved, "STATUS_CHANGED",
                "Status changed from " + oldStatus + " to " + newStatus));
        return DeviceResponse.from(saved);
    }

    public void delete(UUID id) {
        Device device = getOrThrow(id);
        // Log before delete so the log entry is still valid
        logRepo.save(new DeviceLog(device, "DELETED",
                "Device '" + device.getName() + "' deleted"));
        deviceRepo.delete(device);
    }

    private Device getOrThrow(UUID id) {
        return deviceRepo.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }

    private void applyRequest(Device device, DeviceRequest req) {
        device.setName(req.getName());
        device.setType(req.getType());
        device.setStatus(req.getStatus() != null ? req.getStatus() : DeviceStatus.ACTIVE);
        device.setIpAddress(req.getIpAddress());
        device.setLocation(req.getLocation());
    }
}
