package com.mj.portfolio.controller;

import com.mj.portfolio.dto.DeviceRequest;
import com.mj.portfolio.dto.DeviceResponse;
import com.mj.portfolio.entity.enums.DeviceStatus;
import com.mj.portfolio.entity.enums.DeviceType;
import com.mj.portfolio.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Devices", description = "CRUD operations for network devices")
@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @Operation(summary = "List devices", description = "Paginated list with optional filters by status, type, or search query")
    @GetMapping
    public Page<DeviceResponse> list(
            @Parameter(description = "Filter by device status") @RequestParam(required = false) DeviceStatus status,
            @Parameter(description = "Filter by device type") @RequestParam(required = false) DeviceType type,
            @Parameter(description = "Search by name (case-insensitive)") @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.findAll(status, type, q, pageable);
    }

    @Operation(summary = "Get device by ID")
    @GetMapping("/{id}")
    public DeviceResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(summary = "Create a new device")
    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(summary = "Update an existing device")
    @PutMapping("/{id}")
    public DeviceResponse update(@PathVariable UUID id,
                                 @Valid @RequestBody DeviceRequest req) {
        return service.update(id, req);
    }

    @Operation(summary = "Update device status", description = "Partial update: change only the status field")
    @PatchMapping("/{id}/status")
    public DeviceResponse updateStatus(@PathVariable UUID id,
                                       @RequestBody Map<String, String> body) {
        DeviceStatus newStatus = DeviceStatus.valueOf(body.get("status").toUpperCase());
        return service.updateStatus(id, newStatus);
    }

    @Operation(summary = "Delete a device")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
