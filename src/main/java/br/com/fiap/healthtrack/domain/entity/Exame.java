package br.com.fiap.healthtrack.domain.entity;

import br.com.fiap.healthtrack.domain.enums.TipoExame;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "EXAMES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_exame", nullable = false, length = 30)
    private TipoExame tipoExame;

    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;

    @Column(name = "data_realizacao", nullable = false)
    private LocalDate dataRealizacao;

    @Column(name = "resultado", length = 1000)
    private String resultado;

    @Column(name = "laboratorio", length = 200)
    private String laboratorio;
}
