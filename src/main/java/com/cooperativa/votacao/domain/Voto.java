package com.cooperativa.votacao.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@Table(name = "votos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sessao_id", "associado_cpf"})
})
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false)
    private Sessao sessao;

    @Column(name = "associado_cpf", nullable = false)
    private String associadoCpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VotoValor valor;

    public Voto() {}

    public Voto(Sessao sessao, String associadoCpf, VotoValor valor) {
        this.sessao = sessao;
        this.associadoCpf = associadoCpf;
        this.valor = valor;
    }

    public UUID getId() {
        return id;
    }

    public Sessao getSessao() {
        return sessao;
    }

    public void setSessao(Sessao sessao) {
        this.sessao = sessao;
    }

    public String getAssociadoCpf() {
        return associadoCpf;
    }

    public void setAssociadoCpf(String associadoCpf) {
        this.associadoCpf = associadoCpf;
    }

    public VotoValor getValor() {
        return valor;
    }

    public void setValor(VotoValor valor) {
        this.valor = valor;
    }
}
