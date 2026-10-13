package br.com.fiap.esg.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Indicador ESG medido em uma cidade, por exemplo:
 * AMBIENTAL - "Coleta seletiva" = 42.5 %; SOCIAL - "Cobertura de saneamento" = 87 %.
 */
@Entity
@Table(name = "indicadores_esg")
public class IndicadorEsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cidade_id")
    private Cidade cidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Pilar pilar;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private Double valor;

    @Column(length = 20)
    private String unidade;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    protected IndicadorEsg() {
    }

    public IndicadorEsg(Cidade cidade, Pilar pilar, String nome, Double valor, String unidade, LocalDate dataReferencia) {
        this.cidade = cidade;
        this.pilar = pilar;
        this.nome = nome;
        this.valor = valor;
        this.unidade = unidade;
        this.dataReferencia = dataReferencia;
    }

    public Long getId() {
        return id;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public Pilar getPilar() {
        return pilar;
    }

    public void setPilar(Pilar pilar) {
        this.pilar = pilar;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public void setDataReferencia(LocalDate dataReferencia) {
        this.dataReferencia = dataReferencia;
    }
}
