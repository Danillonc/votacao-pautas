package com.cooperativa.votacao.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessaoResponseDTO(
    UUID id,
    UUID pautaId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    boolean isOpen
) {}
