package com.cooperativa.votacao.messaging;

import com.cooperativa.votacao.domain.VotoValor;
import java.util.UUID;

public record VotoMessage(
    UUID sessaoId,
    String associadoCpf,
    VotoValor valor
) {}
