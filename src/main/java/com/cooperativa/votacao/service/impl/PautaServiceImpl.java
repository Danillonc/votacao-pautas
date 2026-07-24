package com.cooperativa.votacao.service.impl;

import com.cooperativa.votacao.domain.Pauta;
import com.cooperativa.votacao.domain.VotoValor;
import com.cooperativa.votacao.dto.PautaRequestDTO;
import com.cooperativa.votacao.dto.PautaResponseDTO;
import com.cooperativa.votacao.dto.ResultadoResponseDTO;
import com.cooperativa.votacao.repository.PautaRepository;
import com.cooperativa.votacao.repository.SessaoRepository;
import com.cooperativa.votacao.repository.VotoRepository;
import com.cooperativa.votacao.service.PautaService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PautaServiceImpl implements PautaService {

    private final PautaRepository pautaRepository;
    private final SessaoRepository sessaoRepository;
    private final VotoRepository votoRepository;

    public PautaServiceImpl(PautaRepository pautaRepository, SessaoRepository sessaoRepository, VotoRepository votoRepository) {
        this.pautaRepository = pautaRepository;
        this.sessaoRepository = sessaoRepository;
        this.votoRepository = votoRepository;
    }

    @Override
    public PautaResponseDTO createPauta(PautaRequestDTO request) {
        Pauta pauta = new Pauta(request.name(), request.description());
        pauta = pautaRepository.save(pauta);
        return new PautaResponseDTO(pauta.getId(), pauta.getName(), pauta.getDescription());
    }

    @Override
    public ResultadoResponseDTO getResultado(UUID pautaId) {
        pautaRepository.findById(pautaId)
                .orElseThrow(() -> new IllegalArgumentException("Pauta not found"));
        
        return sessaoRepository.findByPautaId(pautaId).stream()
                .findFirst()
                .map(sessao -> {
                    long totalSim = votoRepository.countBySessaoIdAndValor(sessao.getId(), VotoValor.SIM);
                    long totalNao = votoRepository.countBySessaoIdAndValor(sessao.getId(), VotoValor.NAO);
                    long totalVotos = votoRepository.countBySessaoId(sessao.getId());
                    String status = sessao.isOpen() ? "OPEN" : "CLOSED";
                    return new ResultadoResponseDTO(pautaId, totalSim, totalNao, totalVotos, status);
                })
                .orElseThrow(() -> new IllegalArgumentException("Sessao not found for Pauta"));
    }
}
