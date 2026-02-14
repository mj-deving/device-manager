package com.mj.portfolio.dto;

import java.util.List;
import java.util.Map;

public class StatsResponse {

    private long total;
    private Map<String, Long> byType;
    private Map<String, Long> byStatus;
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
