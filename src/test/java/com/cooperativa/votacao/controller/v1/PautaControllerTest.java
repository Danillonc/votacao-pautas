package com.cooperativa.votacao.controller.v1;

import com.cooperativa.votacao.config.AppConfig;
import com.cooperativa.votacao.dto.PautaRequestDTO;
import com.cooperativa.votacao.dto.PautaResponseDTO;
import com.cooperativa.votacao.dto.ResultadoResponseDTO;
import com.cooperativa.votacao.dto.SessaoRequestDTO;
import com.cooperativa.votacao.dto.SessaoResponseDTO;
import com.cooperativa.votacao.exception.GlobalExceptionHandler;
import com.cooperativa.votacao.service.PautaService;
import com.cooperativa.votacao.service.SessaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PautaController.class)
@Import({AppConfig.class, GlobalExceptionHandler.class})
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PautaService pautaService;

    @MockitoBean
    private SessaoService sessaoService;

    @Test
    void createPauta_ShouldReturn201() throws Exception {
        PautaRequestDTO req = new PautaRequestDTO("Pauta 1", "Desc");
        PautaResponseDTO res = new PautaResponseDTO(UUID.randomUUID(), "Pauta 1", "Desc");

        when(pautaService.createPauta(any(PautaRequestDTO.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pauta 1"));
    }

    @Test
    void createPauta_NameBlank_ShouldReturn400() throws Exception {
        PautaRequestDTO req = new PautaRequestDTO("", "Desc");

        mockMvc.perform(post("/api/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Validation failed"));
    }

    @Test
    void createPauta_NameNull_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": null, \"description\": \"Desc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void createPauta_EmptyBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void openSessao_ShouldReturn201() throws Exception {
        UUID pautaId = UUID.randomUUID();
        SessaoRequestDTO req = new SessaoRequestDTO(5);
        SessaoResponseDTO res = new SessaoResponseDTO(UUID.randomUUID(), pautaId, LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), true);

        when(sessaoService.openSessao(eq(pautaId), any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/pautas/{id}/sessoes", pautaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isOpen").value(true));
    }

    @Test
    void openSessao_DurationZero_ShouldReturn400() throws Exception {
        UUID pautaId = UUID.randomUUID();
        SessaoRequestDTO req = new SessaoRequestDTO(0);

        mockMvc.perform(post("/api/v1/pautas/{id}/sessoes", pautaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void openSessao_PautaNotFound_ShouldReturn400() throws Exception {
        UUID pautaId = UUID.randomUUID();

        when(sessaoService.openSessao(eq(pautaId), any()))
                .thenThrow(new IllegalArgumentException("Pauta not found"));

        mockMvc.perform(post("/api/v1/pautas/{id}/sessoes", pautaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SessaoRequestDTO(5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Request"))
                .andExpect(jsonPath("$.detail").value("Pauta not found"));
    }

    @Test
    void openSessao_SessaoAlreadyExists_ShouldReturn409() throws Exception {
        UUID pautaId = UUID.randomUUID();

        when(sessaoService.openSessao(eq(pautaId), any()))
                .thenThrow(new IllegalStateException("Sessao already exists for this Pauta"));

        mockMvc.perform(post("/api/v1/pautas/{id}/sessoes", pautaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SessaoRequestDTO(5))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Sessao already exists for this Pauta"));
    }

    @Test
    void getResultado_ShouldReturn200() throws Exception {
        UUID pautaId = UUID.randomUUID();
        ResultadoResponseDTO res = new ResultadoResponseDTO(pautaId, 10L, 5L, 15L, "CLOSED");

        when(pautaService.getResultado(pautaId)).thenReturn(res);

        mockMvc.perform(get("/api/v1/pautas/{id}/resultado", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSim").value(10))
                .andExpect(jsonPath("$.totalNao").value(5))
                .andExpect(jsonPath("$.totalVotos").value(15))
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void getResultado_PautaNotFound_ShouldReturn400() throws Exception {
        UUID pautaId = UUID.randomUUID();

        when(pautaService.getResultado(pautaId))
                .thenThrow(new IllegalArgumentException("Pauta not found"));

        mockMvc.perform(get("/api/v1/pautas/{id}/resultado", pautaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Request"))
                .andExpect(jsonPath("$.detail").value("Pauta not found"));
    }

    @Test
    void getResultado_SessaoNotFound_ShouldReturn400() throws Exception {
        UUID pautaId = UUID.randomUUID();

        when(pautaService.getResultado(pautaId))
                .thenThrow(new IllegalArgumentException("Sessao not found for Pauta"));

        mockMvc.perform(get("/api/v1/pautas/{id}/resultado", pautaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Sessao not found for Pauta"));
    }
}
