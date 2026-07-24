package com.cooperativa.votacao.service;

import com.cooperativa.votacao.dto.SessaoRequestDTO;
import com.cooperativa.votacao.dto.SessaoResponseDTO;

import java.util.UUID;

public interface SessaoService {
    SessaoResponseDTO openSessao(UUID pautaId, SessaoRequestDTO request);
}
