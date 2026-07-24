package com.cooperativa.votacao.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class VotoProducer {

    private final KafkaTemplate<String, VotoMessage> kafkaTemplate;
    private static final String TOPIC = "votos-topic";

    public VotoProducer(KafkaTemplate<String, VotoMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendVoto(VotoMessage votoMessage) {
        kafkaTemplate.send(TOPIC, votoMessage.sessaoId().toString(), votoMessage);
    }
}
