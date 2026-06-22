package com.example.mesweb.inspection.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record InspectionMessage(
	@NotBlank String eventId,
	@NotNull Integer scheduleId,
	@NotBlank String clientId,
    @NotBlank String plantCode,
    @NotBlank String facilityId,
    @Pattern(regexp = "OK|FAIL") String result,
    @NotNull OffsetDateTime inspectedAt
) {}
