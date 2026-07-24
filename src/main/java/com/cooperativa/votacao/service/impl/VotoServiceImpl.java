package com.cooperativa.votacao.service.impl;

import com.cooperativa.votacao.domain.Sessao;
import com.cooperativa.votacao.dto.VotoRequestDTO;
import com.cooperativa.votacao.messaging.VotoMessage;
import com.cooperativa.votacao.messaging.VotoProducer;
import com.cooperativa.votacao.repository.SessaoRepository;
import com.cooperativa.votacao.repository.VotoRepository;
import com.cooperativa.votacao.service.VotoService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VotoServiceImpl implements VotoService {

    private final SessaoRepository sessaoRepository;
    private final VotoRepository votoRepository;
    private final VotoProducer votoProducer;

    public VotoServiceImpl(SessaoRepository sessaoRepository, VotoRepository votoRepository, VotoProducer votoProducer) {
        this.sessaoRepository = sessaoRepository;
        this.votoRepository = votoRepository;
        this.votoProducer = votoProducer;
    }

    @Override
    public void registerVoto(UUID sessaoId, VotoRequestDTO request) {
        Sessao sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new IllegalArgumentException("Sessao not found"));

        if (!sessao.isOpen()) {
            throw new IllegalStateException("Sessao is closed");
        }

        if (votoRepository.existsBySessaoPautaIdAndAssociadoCpf(sessao.getPauta().getId(), request.associadoCpf())) {
            throw new IllegalStateException("Associado already voted in this Pauta");
        }

        VotoMessage message = new VotoMessage(sessaoId, request.associadoCpf(), request.valor());
        votoProducer.sendVoto(message);
    }
}
