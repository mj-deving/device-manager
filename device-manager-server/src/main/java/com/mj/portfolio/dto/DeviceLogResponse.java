package com.mj.portfolio.dto;

import com.mj.portfolio.entity.DeviceLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Audit log entry for a device change")
public class DeviceLogResponse {

    @Schema(description = "Log entry ID")
    private UUID id;
    @Schema(description = "Action performed", example = "CREATED")
    private String action;
    @Schema(description = "Human-readable description of the change")
    private String description;
    @Schema(description = "When the action occurred")
    private LocalDateTime createdAt;

    public static DeviceLogResponse from(DeviceLog log) {
        DeviceLogResponse r = new DeviceLogResponse();
        r.id          = log.getId();
        r.action      = log.getAction();
        r.description = log.getDescription();
        r.createdAt   = log.getCreatedAt();
        return r;
    }

    public UUID getId()              { return id; }
    public String getAction()        { return action; }
    public String getDescription()   { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
