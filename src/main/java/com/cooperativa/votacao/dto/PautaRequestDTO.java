package com.cooperativa.votacao.dto;

import jakarta.validation.constraints.NotBlank;

public record PautaRequestDTO(
    @NotBlank(message = "Name is required")
    String name,
    String description
) {}
