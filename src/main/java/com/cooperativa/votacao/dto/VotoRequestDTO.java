package com.cooperativa.votacao.dto;

import com.cooperativa.votacao.domain.VotoValor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VotoRequestDTO(
    @NotBlank(message = "Associado CPF is required")
    @org.hibernate.validator.constraints.br.CPF(message = "CPF invalid format")
    @jakarta.validation.constraints.Pattern(regexp = "(^\\d{3}\\.\\d{3}\\.\\d{3}\\-\\d{2}$)|(^\\d{11}$)", message = "CPF must contain 11 digits or valid format")
    String associadoCpf,
    
    @NotNull(message = "Valor is required (SIM or NAO)")
    VotoValor valor
) {}
