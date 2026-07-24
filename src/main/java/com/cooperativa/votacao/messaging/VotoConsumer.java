package com.cooperativa.votacao.messaging;

import com.cooperativa.votacao.domain.Sessao;
import com.cooperativa.votacao.domain.Voto;
import com.cooperativa.votacao.integration.UserInfoClient;
import com.cooperativa.votacao.repository.SessaoRepository;
import com.cooperativa.votacao.repository.VotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VotoConsumer {

    private static final Logger log = LoggerFactory.getLogger(VotoConsumer.class);

    private final VotoRepository votoRepository;
    private final SessaoRepository sessaoRepository;
    private final UserInfoClient userInfoClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VotoConsumer(VotoRepository votoRepository, SessaoRepository sessaoRepository, UserInfoClient userInfoClient) {
        this.votoRepository = votoRepository;
        this.sessaoRepository = sessaoRepository;
        this.userInfoClient = userInfoClient;
    }

    @KafkaListener(topics = "votos-topic", groupId = "votacao-group")
    @Transactional
    public void consume(String messagePayload, Acknowledgment ack) {
        VotoMessage message;
        try {
            message = objectMapper.readValue(messagePayload, VotoMessage.class);
        } catch (Exception e) {
            log.error("Failed to parse message: {}", messagePayload, e);
            ack.acknowledge();
            return;
        }

        Sessao sessao = sessaoRepository.findById(message.sessaoId()).orElse(null);
        if (sessao == null || !sessao.isOpen()) {
            log.warn("Sessao {} is not open or not found.", message.sessaoId());
            ack.acknowledge();
            return;
        }

        if (votoRepository.existsBySessaoPautaIdAndAssociadoCpf(sessao.getPauta().getId(), message.associadoCpf())) {
            log.warn("Associado {} already voted in pauta {}", "***", sessao.getPauta().getId());
            ack.acknowledge();
            return;
        }

        if (!userInfoClient.isAbleToVote(message.associadoCpf())) {
            log.warn("Associado {} is unable to vote.", "***");
            ack.acknowledge();
            return;
        }

        Voto voto = new Voto(sessao, message.associadoCpf(), message.valor());
        votoRepository.save(voto);
        log.info("Vote saved for sessao {}", message.sessaoId());
        
        ack.acknowledge();
    }
}
