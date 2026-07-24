package com.cooperativa.votacao.integration;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserInfoClient {

    private final RestTemplate restTemplate;
    private final String url;

    public UserInfoClient(RestTemplate restTemplate, @Value("${app.integration.user-info.url}") String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    @CircuitBreaker(name = "userInfo", fallbackMethod = "fallbackCpfValidation")
    @Retry(name = "userInfo", fallbackMethod = "fallbackCpfValidation")
    public boolean isAbleToVote(String cpf) {
        try {
            CpfValidationResponse response = restTemplate.getForObject(url + "/" + cpf, CpfValidationResponse.class);
            return response != null && response.status() == CpfStatus.ABLE_TO_VOTE;
        } catch (HttpClientErrorException.NotFound e) {
            return true;
        }
    }

    public boolean fallbackCpfValidation(String cpf, Throwable t) {
        return true;
    }
}
