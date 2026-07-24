package com.cooperativa.votacao.repository;

import com.cooperativa.votacao.domain.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {
    List<Sessao> findByPautaId(UUID pautaId);
}
