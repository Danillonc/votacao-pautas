package com.cooperativa.votacao.service;

import com.cooperativa.votacao.dto.VotoRequestDTO;

import java.util.UUID;

public interface VotoService {
    void registerVoto(UUID sessaoId, VotoRequestDTO request);
}
