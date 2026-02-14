package com.mj.portfolio.service;

import com.mj.portfolio.dto.DeviceRequest;
import com.mj.portfolio.dto.DeviceResponse;
import com.mj.portfolio.entity.Device;
import com.mj.portfolio.entity.enums.DeviceStatus;
import com.mj.portfolio.entity.enums.DeviceType;
import com.mj.portfolio.exception.DeviceNotFoundException;
import com.mj.portfolio.repository.DeviceLogRepository;
import com.mj.portfolio.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock DeviceRepository deviceRepo;
    @Mock DeviceLogRepository logRepo;

    @InjectMocks DeviceService service;

    private Device sampleDevice;
    private UUID sampleId;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();
        sampleDevice = new Device();
        sampleDevice.setName("Router-1");
        sampleDevice.setType(DeviceType.ROUTER);
        sampleDevice.setStatus(DeviceStatus.ACTIVE);
        sampleDevice.setIpAddress("192.168.1.1");
        sampleDevice.setLocation("Server Room");
    }

    @Test
    void findAll_returnsPagedResults() {
        Page<Device> page = new PageImpl<>(List.of(sampleDevice));
        when(deviceRepo.findAll(any(PageRequest.class))).thenReturn(page);

        Page<DeviceResponse> result = service.findAll(null, null, null, PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Router-1");
    }

    @Test
    void findById_returnsDevice_whenExists() {
        when(deviceRepo.findById(sampleId)).thenReturn(Optional.of(sampleDevice));

        DeviceResponse response = service.findById(sampleId);

        assertThat(response.getName()).isEqualTo("Router-1");
        assertThat(response.getType()).isEqualTo(DeviceType.ROUTER);
    }

    @Test
    void findById_throws_whenNotFound() {
        when(deviceRepo.findById(sampleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(sampleId))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    @Test
    void create_savesDeviceAndLog() {
        DeviceRequest req = new DeviceRequest();
        req.setName("Server-A");
        req.setType(DeviceType.SERVER);
        req.setStatus(DeviceStatus.ACTIVE);

        Device saved = new Device();
        saved.setName("Server-A");
        saved.setType(DeviceType.SERVER);
        saved.setStatus(DeviceStatus.ACTIVE);

        when(deviceRepo.save(any(Device.class))).thenReturn(saved);

        DeviceResponse result = service.create(req);

        assertThat(result.getName()).isEqualTo("Server-A");
        verify(logRepo).save(argThat(log -> "CREATED".equals(log.getAction())));
    }

    @Test
    void update_updatesFieldsAndLogs() {
        when(deviceRepo.findById(sampleId)).thenReturn(Optional.of(sampleDevice));
        when(deviceRepo.save(any(Device.class))).thenReturn(sampleDevice);

        DeviceRequest req = new DeviceRequest();
        req.setName("Router-Updated");
        req.setType(DeviceType.ROUTER);
        req.setStatus(DeviceStatus.MAINTENANCE);

        service.update(sampleId, req);

        verify(logRepo, atLeastOnce()).save(argThat(log -> "UPDATED".equals(log.getAction())));
    }

    @Test
    void delete_removesDeviceAndLogs() {
        when(deviceRepo.findById(sampleId)).thenReturn(Optional.of(sampleDevice));

        service.delete(sampleId);

        verify(logRepo).save(argThat(log -> "DELETED".equals(log.getAction())));
        verify(deviceRepo).delete(sampleDevice);
    }
}
