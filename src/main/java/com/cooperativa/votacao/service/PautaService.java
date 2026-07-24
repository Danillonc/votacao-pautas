package com.cooperativa.votacao.service;

import com.cooperativa.votacao.dto.PautaRequestDTO;
import com.cooperativa.votacao.dto.PautaResponseDTO;
import com.cooperativa.votacao.dto.ResultadoResponseDTO;

import java.util.UUID;

public interface PautaService {
    PautaResponseDTO createPauta(PautaRequestDTO request);
    ResultadoResponseDTO getResultado(UUID pautaId);
}
