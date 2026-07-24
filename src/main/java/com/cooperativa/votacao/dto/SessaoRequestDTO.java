package com.cooperativa.votacao.dto;

import jakarta.validation.constraints.Min;

public record SessaoRequestDTO(
    @Min(value = 1, message = "Duration must be at least 1 minute if provided")
    Integer durationInMinutes
) {}
