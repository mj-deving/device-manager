package com.mj.portfolio.controller;

import com.mj.portfolio.dto.DeviceLogResponse;
import com.mj.portfolio.repository.DeviceLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/devices/{id}/logs")
public class DeviceLogController {

    private final DeviceLogRepository logRepo;

    public DeviceLogController(DeviceLogRepository logRepo) {
        this.logRepo = logRepo;
    }

    @GetMapping
    public List<DeviceLogResponse> getLogs(@PathVariable UUID id) {
        return logRepo.findByDeviceIdOrderByCreatedAtDesc(id)
                .stream()
                .map(DeviceLogResponse::from)
                .collect(Collectors.toList());
    }
}
