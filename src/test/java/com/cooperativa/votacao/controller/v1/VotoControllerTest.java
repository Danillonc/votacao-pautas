package com.cooperativa.votacao.controller.v1;

import com.cooperativa.votacao.config.AppConfig;
import com.cooperativa.votacao.domain.VotoValor;
import com.cooperativa.votacao.dto.VotoRequestDTO;
import com.cooperativa.votacao.exception.GlobalExceptionHandler;
import com.cooperativa.votacao.service.VotoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VotoController.class)
@Import({AppConfig.class, GlobalExceptionHandler.class})
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VotoService votoService;

    @Test
    void registerVoto_ShouldReturn202() throws Exception {
        UUID sessaoId = UUID.randomUUID();
        VotoRequestDTO req = new VotoRequestDTO("52998224725", VotoValor.SIM);

        doNothing().when(votoService).registerVoto(eq(sessaoId), any(VotoRequestDTO.class));

        mockMvc.perform(post("/api/v1/sessoes/{id}/votos", sessaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isAccepted());
    }

    @Test
    void registerVoto_CpfBlank_ShouldReturn400() throws Exception {
        UUID sessaoId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/sessoes/{id}/votos", sessaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"associadoCpf\": \"\", \"valor\": \"SIM\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Validation failed"));
    }

    @Test
    void registerVoto_CpfInvalid_ShouldReturn400() throws Exception {
        UUID sessaoId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/sessoes/{id}/votos", sessaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"associadoCpf\": \"12345678901\", \"valor\": \"SIM\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void registerVoto_ValorNull_ShouldReturn400() throws Exception {
        UUID sessaoId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/sessoes/{id}/votos", sessaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"associadoCpf\": \"52998224725\", \"valor\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void registerVoto_EmptyBody_ShouldReturn400() throws Exception {
        UUID sessaoId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/sessoes/{id}/votos", sessaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerVoto_SessaoClosed_ShouldReturn409() throws Exception {
        UUID sessaoId = UUID.randomUUID();
        VotoRequestDTO req = new VotoRequestDTO("52998224725", VotoValor.SIM);

        doThrow(new IllegalStateException("Sessao is closed"))
                .when(votoService).registerVoto(eq(sessaoId), any(VotoRequestDTO.class));

        mockMvc.perform(post("/api/v1/sessoes/{id}/votos", sessaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Sessao is closed"));
    }

    @Test
    void registerVoto_AssociadoAlreadyVoted_ShouldReturn409() throws Exception {
        UUID sessaoId = UUID.randomUUID();
        VotoRequestDTO req = new VotoRequestDTO("52998224725", VotoValor.SIM);

        doThrow(new IllegalStateException("Associado already voted in this Pauta"))
                .when(votoService).registerVoto(eq(sessaoId), any(VotoRequestDTO.class));

        mockMvc.perform(post("/api/v1/sessoes/{id}/votos", sessaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Associado already voted in this Pauta"));
    }

    @Test
    void registerVoto_SessaoNotFound_ShouldReturn400() throws Exception {
        UUID sessaoId = UUID.randomUUID();
        VotoRequestDTO req = new VotoRequestDTO("52998224725", VotoValor.SIM);

        doThrow(new IllegalArgumentException("Sessao not found"))
                .when(votoService).registerVoto(eq(sessaoId), any(VotoRequestDTO.class));

        mockMvc.perform(post("/api/v1/sessoes/{id}/votos", sessaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Request"))
                .andExpect(jsonPath("$.detail").value("Sessao not found"));
    }
}
