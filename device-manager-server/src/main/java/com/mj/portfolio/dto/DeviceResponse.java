package com.mj.portfolio.dto;

import com.mj.portfolio.entity.Device;
import com.mj.portfolio.entity.enums.DeviceStatus;
import com.mj.portfolio.entity.enums.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Device details returned by the API")
public class DeviceResponse {

    @Schema(description = "Unique device identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID id;
    @Schema(description = "Device name", example = "Switch-Floor3")
    private String name;
    @Schema(description = "Device type", example = "SWITCH")
    private DeviceType type;
    @Schema(description = "Current status", example = "ACTIVE")
    private DeviceStatus status;
    @Schema(description = "IP address", example = "192.168.1.100")
    private String ipAddress;
    @Schema(description = "Physical location", example = "Building A, Floor 3")
    private String location;
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    @Schema(description = "Last update timestamp")
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
