package com.cooperativa.votacao.service.impl;

import com.cooperativa.votacao.domain.Pauta;
import com.cooperativa.votacao.domain.Sessao;
import com.cooperativa.votacao.dto.SessaoRequestDTO;
import com.cooperativa.votacao.dto.SessaoResponseDTO;
import com.cooperativa.votacao.repository.PautaRepository;
import com.cooperativa.votacao.repository.SessaoRepository;
import com.cooperativa.votacao.service.SessaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessaoServiceImpl implements SessaoService {

    private static final Logger log = LoggerFactory.getLogger(SessaoServiceImpl.class);

    private final SessaoRepository sessaoRepository;
    private final PautaRepository pautaRepository;

    public SessaoServiceImpl(SessaoRepository sessaoRepository, PautaRepository pautaRepository) {
        this.sessaoRepository = sessaoRepository;
        this.pautaRepository = pautaRepository;
    }

    @Override
    public SessaoResponseDTO openSessao(UUID pautaId, SessaoRequestDTO request) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new IllegalArgumentException("Pauta not found"));

        if (!sessaoRepository.findByPautaId(pautaId).isEmpty()) {
            throw new IllegalStateException("Sessao already exists for this Pauta");
        }

        int duration = (request != null && request.durationInMinutes() != null) ? request.durationInMinutes() : 1;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(duration);

        Sessao sessao = new Sessao(pauta, startTime, endTime);
        sessao = sessaoRepository.save(sessao);
        log.info("Sessao opened for Pauta ID: {}, ends at: {}", pautaId, endTime);

        return new SessaoResponseDTO(
                sessao.getId(),
                sessao.getPauta().getId(),
                sessao.getStartTime(),
                sessao.getEndTime(),
                sessao.isOpen()
        );
    }
}
