package com.cooperativa.votacao.controller.v1;

import com.cooperativa.votacao.dto.VotoRequestDTO;
import com.cooperativa.votacao.service.VotoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessoes")
public class VotoController {

    private static final Logger log = LoggerFactory.getLogger(VotoController.class);

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping("/{id}/votos")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void registerVoto(@PathVariable UUID id, @Valid @RequestBody VotoRequestDTO request) {
        log.info("Receiving request to register vote for Sessao ID: {}", id);
        votoService.registerVoto(id, request);
    }
}
