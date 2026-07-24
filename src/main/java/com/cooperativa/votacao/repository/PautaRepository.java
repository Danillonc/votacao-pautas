package com.cooperativa.votacao.repository;

import com.cooperativa.votacao.domain.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PautaRepository extends JpaRepository<Pauta, UUID> {
}
