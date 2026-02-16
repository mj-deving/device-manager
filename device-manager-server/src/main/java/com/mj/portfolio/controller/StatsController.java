package com.mj.portfolio.controller;

import com.mj.portfolio.dto.StatsResponse;
import com.mj.portfolio.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Statistics", description = "Dashboard statistics and summaries")
@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @Operation(summary = "Get dashboard statistics", description = "Returns device counts by type/status and recent activity")
    @GetMapping
    public StatsResponse getStats() {
        return service.getStats();
    }
}
