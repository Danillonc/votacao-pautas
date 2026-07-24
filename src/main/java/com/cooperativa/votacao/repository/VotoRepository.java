package com.cooperativa.votacao.repository;

import com.cooperativa.votacao.domain.Voto;
import com.cooperativa.votacao.domain.VotoValor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface VotoRepository extends JpaRepository<Voto, UUID> {
    boolean existsBySessaoIdAndAssociadoCpf(UUID sessaoId, String associadoCpf);
    boolean existsBySessaoPautaIdAndAssociadoCpf(UUID pautaId, String associadoCpf);
    long countBySessaoIdAndValor(UUID sessaoId, VotoValor valor);
    long countBySessaoId(UUID sessaoId);
}
