package com.cooperativa.votacao.service.impl;

import com.cooperativa.votacao.domain.Pauta;
import com.cooperativa.votacao.domain.Sessao;
import com.cooperativa.votacao.domain.VotoValor;
import com.cooperativa.votacao.dto.VotoRequestDTO;
import com.cooperativa.votacao.messaging.VotoMessage;
import com.cooperativa.votacao.messaging.VotoProducer;
import com.cooperativa.votacao.repository.SessaoRepository;
import com.cooperativa.votacao.repository.VotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotoServiceImplTest {

    @Mock
    private SessaoRepository sessaoRepository;
    @Mock
    private VotoRepository votoRepository;
    @Mock
    private VotoProducer votoProducer;

    @InjectMocks
    private VotoServiceImpl votoService;

    @Test
    void registerVoto_Success() {
        UUID sessaoId = UUID.randomUUID();
        Pauta pauta = new Pauta("Pauta 1", "Desc");
        Sessao sessao = new Sessao(pauta, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(5));
        VotoRequestDTO req = new VotoRequestDTO("52998224725", VotoValor.SIM);

        when(sessaoRepository.findById(sessaoId)).thenReturn(Optional.of(sessao));
        when(votoRepository.existsBySessaoPautaIdAndAssociadoCpf(isNull(), eq("52998224725"))).thenReturn(false);

        assertDoesNotThrow(() -> votoService.registerVoto(sessaoId, req));
        verify(votoProducer, times(1)).sendVoto(any(VotoMessage.class));
    }

    @Test
    void registerVoto_SessaoNotFound_ThrowsException() {
        UUID sessaoId = UUID.randomUUID();
        when(sessaoRepository.findById(sessaoId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> votoService.registerVoto(sessaoId, new VotoRequestDTO("52998224725", VotoValor.SIM))
        );
        assertEquals("Sessao not found", ex.getMessage());
    }

    @Test
    void registerVoto_SessaoClosed_ThrowsException() {
        UUID sessaoId = UUID.randomUUID();
        Pauta pauta = new Pauta("Pauta 1", "Desc");
        Sessao sessao = new Sessao(pauta, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().minusMinutes(5));

        when(sessaoRepository.findById(sessaoId)).thenReturn(Optional.of(sessao));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> votoService.registerVoto(sessaoId, new VotoRequestDTO("52998224725", VotoValor.SIM))
        );
        assertEquals("Sessao is closed", ex.getMessage());
    }

    @Test
    void registerVoto_AssociadoAlreadyVoted_ThrowsException() {
        UUID sessaoId = UUID.randomUUID();
        Pauta pauta = new Pauta("Pauta 1", "Desc");
        Sessao sessao = new Sessao(pauta, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(5));
        VotoRequestDTO req = new VotoRequestDTO("52998224725", VotoValor.SIM);

        when(sessaoRepository.findById(sessaoId)).thenReturn(Optional.of(sessao));
        when(votoRepository.existsBySessaoPautaIdAndAssociadoCpf(isNull(), eq("52998224725"))).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> votoService.registerVoto(sessaoId, req)
        );
        assertEquals("Associado already voted in this Pauta", ex.getMessage());
    }
}
