package com.cooperativa.votacao.dto;

import java.util.UUID;

public record ResultadoResponseDTO(
    UUID pautaId,
    long totalSim,
    long totalNao,
    long totalVotos,
    String status
) {}
