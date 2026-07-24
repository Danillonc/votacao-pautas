package com.cooperativa.votacao.controller.v1;

import com.cooperativa.votacao.dto.PautaRequestDTO;
import com.cooperativa.votacao.dto.PautaResponseDTO;
import com.cooperativa.votacao.dto.ResultadoResponseDTO;
import com.cooperativa.votacao.dto.SessaoRequestDTO;
import com.cooperativa.votacao.dto.SessaoResponseDTO;
import com.cooperativa.votacao.service.PautaService;
import com.cooperativa.votacao.service.SessaoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {

    private static final Logger log = LoggerFactory.getLogger(PautaController.class);

    private final PautaService pautaService;
    private final SessaoService sessaoService;

    public PautaController(PautaService pautaService, SessaoService sessaoService) {
        this.pautaService = pautaService;
        this.sessaoService = sessaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PautaResponseDTO createPauta(@Valid @RequestBody PautaRequestDTO request) {
        log.info("Receiving request to create Pauta: {}", request.name());
        return pautaService.createPauta(request);
    }

    @PostMapping("/{id}/sessoes")
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoResponseDTO openSessao(@PathVariable UUID id, @Valid @RequestBody(required = false) SessaoRequestDTO request) {
        log.info("Receiving request to open Sessao for Pauta ID: {}", id);
        return sessaoService.openSessao(id, request);
    }

    @GetMapping("/{id}/resultado")
    public ResultadoResponseDTO getResultado(@PathVariable UUID id) {
        log.info("Receiving request to get result for Pauta ID: {}", id);
        return pautaService.getResultado(id);
    }
}
