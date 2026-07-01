package com.example.mesweb.monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SimulatorInspectionRequest(
        @NotNull Integer scheduleId,
        @NotBlank String clientId,
        @Pattern(regexp = "OK|FAIL") String result
) {
}
