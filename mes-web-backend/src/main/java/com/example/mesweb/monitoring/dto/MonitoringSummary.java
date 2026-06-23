package com.example.mesweb.monitoring.dto;

public record MonitoringSummary(
        int schIdx,
        String plantCode,
        String plantName,
        String schDate,
        int loadTime,
        String facilityId,
        String facilityName,
        int schAmount,
        long successAmount,
        long failAmount,
        String successRate
) {
}
