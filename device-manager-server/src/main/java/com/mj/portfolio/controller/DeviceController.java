package com.mj.portfolio.controller;

import com.mj.portfolio.dto.DeviceRequest;
import com.mj.portfolio.dto.DeviceResponse;
import com.mj.portfolio.entity.enums.DeviceStatus;
import com.mj.portfolio.entity.enums.DeviceType;
import com.mj.portfolio.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @GetMapping
    public Page<DeviceResponse> list(
            @RequestParam(required = false) DeviceStatus status,
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.findAll(status, type, q, pageable);
    }

    @GetMapping("/{id}")
    public DeviceResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public DeviceResponse update(@PathVariable UUID id,
                                 @Valid @RequestBody DeviceRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/status")
    public DeviceResponse updateStatus(@PathVariable UUID id,
                                       @RequestBody Map<String, String> body) {
        DeviceStatus newStatus = DeviceStatus.valueOf(body.get("status").toUpperCase());
        return service.updateStatus(id, newStatus);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
