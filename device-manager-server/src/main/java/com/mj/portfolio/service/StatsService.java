package com.mj.portfolio.service;

import com.mj.portfolio.dto.DeviceLogResponse;
import com.mj.portfolio.dto.StatsResponse;
import com.mj.portfolio.entity.enums.DeviceStatus;
import com.mj.portfolio.entity.enums.DeviceType;
import com.mj.portfolio.repository.DeviceLogRepository;
import com.mj.portfolio.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private final DeviceRepository deviceRepo;
    private final DeviceLogRepository logRepo;

    public StatsService(DeviceRepository deviceRepo, DeviceLogRepository logRepo) {
        this.deviceRepo = deviceRepo;
        this.logRepo    = logRepo;
    }

    public StatsResponse getStats() {
        long total = deviceRepo.count();

        Map<String, Long> byType = Arrays.stream(DeviceType.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        deviceRepo::countByType,
                        (a, b) -> a,
                        LinkedHashMap::new));

        Map<String, Long> byStatus = Arrays.stream(DeviceStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        deviceRepo::countByStatus,
                        (a, b) -> a,
                        LinkedHashMap::new));

        List<DeviceLogResponse> recent = logRepo.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(DeviceLogResponse::from)
                .collect(Collectors.toList());

        return new StatsResponse(total, byType, byStatus, recent);
    }
}
