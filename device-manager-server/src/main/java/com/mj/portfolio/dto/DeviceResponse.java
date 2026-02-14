package com.mj.portfolio.dto;

import com.mj.portfolio.entity.Device;
import com.mj.portfolio.entity.enums.DeviceStatus;
import com.mj.portfolio.entity.enums.DeviceType;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeviceResponse {

    private UUID id;
    private String name;
    private DeviceType type;
    private DeviceStatus status;
    private String ipAddress;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DeviceResponse from(Device d) {
        DeviceResponse r = new DeviceResponse();
        r.id        = d.getId();
        r.name      = d.getName();
        r.type      = d.getType();
        r.status    = d.getStatus();
        r.ipAddress = d.getIpAddress();
        r.location  = d.getLocation();
        r.createdAt = d.getCreatedAt();
        r.updatedAt = d.getUpdatedAt();
        return r;
    }

    public UUID getId()              { return id; }
    public String getName()          { return name; }
    public DeviceType getType()      { return type; }
    public DeviceStatus getStatus()  { return status; }
    public String getIpAddress()     { return ipAddress; }
    public String getLocation()      { return location; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
