package com.mj.portfolio.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_logs")
public class DeviceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // nullable = true: when a device is deleted, device_id is set to NULL (ON DELETE SET NULL)
    // so deletion audit logs are preserved as permanent history.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "device_id", nullable = true)
    private Device device;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DeviceLog() {}

    public DeviceLog(Device device, String action, String description) {
        this.device = device;
        this.action = action;
        this.description = description;
    }

    public UUID getId() { return id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
