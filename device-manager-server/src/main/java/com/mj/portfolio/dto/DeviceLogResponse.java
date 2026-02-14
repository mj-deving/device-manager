package com.mj.portfolio.dto;

import com.mj.portfolio.entity.DeviceLog;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeviceLogResponse {

    private UUID id;
    private String action;
    private String description;
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
