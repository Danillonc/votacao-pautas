package com.cooperativa.votacao.service.impl;

import com.cooperativa.votacao.domain.Pauta;
import com.cooperativa.votacao.domain.Sessao;
import com.cooperativa.votacao.domain.VotoValor;
import com.cooperativa.votacao.dto.PautaRequestDTO;
import com.cooperativa.votacao.dto.PautaResponseDTO;
import com.cooperativa.votacao.dto.ResultadoResponseDTO;
import com.cooperativa.votacao.repository.PautaRepository;
import com.cooperativa.votacao.repository.SessaoRepository;
import com.cooperativa.votacao.repository.VotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PautaServiceImplTest {

    @Mock
    private PautaRepository pautaRepository;
    @Mock
    private SessaoRepository sessaoRepository;
    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private PautaServiceImpl pautaService;

    @Test
    void createPauta_Success() {
        PautaRequestDTO req = new PautaRequestDTO("Pauta 1", "Desc");
        Pauta pauta = new Pauta("Pauta 1", "Desc");
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);

        PautaResponseDTO res = pautaService.createPauta(req);

        assertNotNull(res);
        assertEquals("Pauta 1", res.name());
        assertEquals("Desc", res.description());
        verify(pautaRepository, times(1)).save(any(Pauta.class));
    }

    @Test
    void getResultado_SessaoOpen_Success() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = new Pauta("Pauta 1", "Desc");
        Sessao sessao = new Sessao(pauta, LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusMinutes(5));

        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPautaId(pautaId)).thenReturn(List.of(sessao));
        when(votoRepository.countBySessaoIdAndValor(isNull(), eq(VotoValor.SIM))).thenReturn(10L);
        when(votoRepository.countBySessaoIdAndValor(isNull(), eq(VotoValor.NAO))).thenReturn(5L);
        when(votoRepository.countBySessaoId(isNull())).thenReturn(15L);

        ResultadoResponseDTO res = pautaService.getResultado(pautaId);

        assertNotNull(res);
        assertEquals(10L, res.totalSim());
        assertEquals(5L, res.totalNao());
        assertEquals(15L, res.totalVotos());
        assertEquals("OPEN", res.status());
    }

    @Test
    void getResultado_SessaoClosed_ReturnsClosedStatus() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = new Pauta("Pauta 1", "Desc");
        Sessao sessao = new Sessao(pauta, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().minusMinutes(5));

        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPautaId(pautaId)).thenReturn(List.of(sessao));
        when(votoRepository.countBySessaoIdAndValor(isNull(), eq(VotoValor.SIM))).thenReturn(3L);
        when(votoRepository.countBySessaoIdAndValor(isNull(), eq(VotoValor.NAO))).thenReturn(7L);
        when(votoRepository.countBySessaoId(isNull())).thenReturn(10L);

        ResultadoResponseDTO res = pautaService.getResultado(pautaId);

        assertNotNull(res);
        assertEquals(3L, res.totalSim());
        assertEquals(7L, res.totalNao());
        assertEquals("CLOSED", res.status());
    }

    @Test
    void getResultado_PautaNotFound_ThrowsException() {
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pautaService.getResultado(pautaId)
        );
        assertEquals("Pauta not found", ex.getMessage());
    }

    @Test
    void getResultado_SessaoNotFound_ThrowsException() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = new Pauta("Pauta 1", "Desc");
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPautaId(pautaId)).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pautaService.getResultado(pautaId)
        );
        assertEquals("Sessao not found for Pauta", ex.getMessage());
    }
}
