package com.cooperativa.votacao.dto;

import java.util.UUID;

public record PautaResponseDTO(
    UUID id,
    String name,
    String description
) {}
