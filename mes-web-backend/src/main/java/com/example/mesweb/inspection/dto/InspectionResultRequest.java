package com.example.mesweb.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InspectionResultRequest(
        @NotNull Integer schIdx,
        @NotBlank String clientId,
        @NotBlank String result,
        String timestamp
) {
}