package com.mj.portfolio.dto;

import com.mj.portfolio.entity.enums.DeviceStatus;
import com.mj.portfolio.entity.enums.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating or updating a device")
public class DeviceRequest {

    @Schema(description = "Device name", example = "Switch-Floor3")
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Device type", example = "SWITCH")
    @NotNull(message = "Type is required")
    private DeviceType type;

    @Schema(description = "Current device status", example = "ACTIVE")
    private DeviceStatus status = DeviceStatus.ACTIVE;

    @Schema(description = "Device IP address", example = "192.168.1.100")
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    private String ipAddress;

    @Schema(description = "Physical location", example = "Building A, Floor 3")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DeviceType getType() { return type; }
    public void setType(DeviceType type) { this.type = type; }

    public DeviceStatus getStatus() { return status; }
    public void setStatus(DeviceStatus status) { this.status = status; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
