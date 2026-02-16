package com.mj.portfolio.controller;

import com.mj.portfolio.dto.DeviceLogResponse;
import com.mj.portfolio.repository.DeviceLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Device Logs", description = "Audit trail for device changes")
@RestController
@RequestMapping("/api/v1/devices/{id}/logs")
public class DeviceLogController {

    private final DeviceLogRepository logRepo;

    public DeviceLogController(DeviceLogRepository logRepo) {
        this.logRepo = logRepo;
    }

    @Operation(summary = "Get device logs", description = "Returns change history for a specific device, newest first")
    @GetMapping
    public List<DeviceLogResponse> getLogs(@PathVariable UUID id) {
        return logRepo.findByDeviceIdOrderByCreatedAtDesc(id)
                .stream()
                .map(DeviceLogResponse::from)
                .collect(Collectors.toList());
    }
}
