package com.mj.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Dashboard statistics overview")
public class StatsResponse {

    @Schema(description = "Total number of devices", example = "42")
    private long total;
    @Schema(description = "Device count grouped by type")
    private Map<String, Long> byType;
    @Schema(description = "Device count grouped by status")
    private Map<String, Long> byStatus;
    @Schema(description = "Most recent device log entries")
    private List<DeviceLogResponse> recentActivity;

    public StatsResponse() {}

    public StatsResponse(long total,
                         Map<String, Long> byType,
                         Map<String, Long> byStatus,
                         List<DeviceLogResponse> recentActivity) {
        this.total          = total;
        this.byType         = byType;
        this.byStatus       = byStatus;
        this.recentActivity = recentActivity;
    }

    public long getTotal()                           { return total; }
    public Map<String, Long> getByType()             { return byType; }
    public Map<String, Long> getByStatus()           { return byStatus; }
    public List<DeviceLogResponse> getRecentActivity() { return recentActivity; }
}
