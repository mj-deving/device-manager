package com.mj.portfolio.repository;

import com.mj.portfolio.entity.Device;
import com.mj.portfolio.entity.enums.DeviceStatus;
import com.mj.portfolio.entity.enums.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Page<Device> findByStatus(DeviceStatus status, Pageable pageable);

    Page<Device> findByType(DeviceType type, Pageable pageable);

    Page<Device> findByStatusAndType(DeviceStatus status, DeviceType type, Pageable pageable);

    @Query("SELECT d FROM Device d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(d.location) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(d.ipAddress) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Device> search(@Param("q") String query, Pageable pageable);

    long countByStatus(DeviceStatus status);

    long countByType(DeviceType type);
}
