package com.example.mesweb.monitoring.dto;

public record ControlMessage(
        String clientId,
        String plantCode,
        String facilityId,
        String timestamp,
        String flag
) {
}
