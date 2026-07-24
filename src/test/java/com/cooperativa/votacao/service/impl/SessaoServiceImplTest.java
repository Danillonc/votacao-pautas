package com.cooperativa.votacao.service.impl;

import com.cooperativa.votacao.domain.Pauta;
import com.cooperativa.votacao.domain.Sessao;
import com.cooperativa.votacao.dto.SessaoRequestDTO;
import com.cooperativa.votacao.dto.SessaoResponseDTO;
import com.cooperativa.votacao.repository.PautaRepository;
import com.cooperativa.votacao.repository.SessaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessaoServiceImplTest {

    @Mock
    private SessaoRepository sessaoRepository;
    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private SessaoServiceImpl sessaoService;

    @Test
    void openSessao_Success() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = new Pauta("Pauta 1", "Desc");
        SessaoRequestDTO req = new SessaoRequestDTO(5);

        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPautaId(pautaId)).thenReturn(List.of());
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(i -> i.getArguments()[0]);

        SessaoResponseDTO res = sessaoService.openSessao(pautaId, req);

        assertNotNull(res);
        assertTrue(res.isOpen());
        verify(sessaoRepository, times(1)).save(any(Sessao.class));
    }

    @Test
    void openSessao_PautaNotFound_ThrowsException() {
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> sessaoService.openSessao(pautaId, new SessaoRequestDTO(5)));
    }

    @Test
    void openSessao_SessaoAlreadyExists_ThrowsException() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = new Pauta("Pauta 1", "Desc");
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPautaId(pautaId)).thenReturn(List.of(new Sessao()));

        assertThrows(IllegalStateException.class, () -> sessaoService.openSessao(pautaId, new SessaoRequestDTO(5)));
    }
}
