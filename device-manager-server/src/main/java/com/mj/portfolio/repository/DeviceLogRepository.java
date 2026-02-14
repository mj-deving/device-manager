package com.mj.portfolio.repository;

import com.mj.portfolio.entity.DeviceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceLogRepository extends JpaRepository<DeviceLog, UUID> {

    List<DeviceLog> findByDeviceIdOrderByCreatedAtDesc(UUID deviceId);

    List<DeviceLog> findTop10ByOrderByCreatedAtDesc();
}
