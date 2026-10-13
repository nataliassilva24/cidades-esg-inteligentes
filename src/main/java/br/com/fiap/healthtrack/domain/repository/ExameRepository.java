package br.com.fiap.healthtrack.domain.repository;

import br.com.fiap.healthtrack.domain.entity.Exame;
import br.com.fiap.healthtrack.domain.enums.TipoExame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExameRepository extends JpaRepository<Exame, Long> {

    Page<Exame> findByPacienteId(Long pacienteId, Pageable pageable);

    @Query("""
        SELECT e.tipoExame, COUNT(e) as total
        FROM Exame e
        WHERE e.dataRealizacao >= :dataInicio
        GROUP BY e.paciente.id, e.tipoExame
        HAVING COUNT(e) > 1
        """)
    List<Object[]> findExamesDuplicadosDesde(@Param("dataInicio") LocalDate dataInicio);

    @Query("""
        SELECT COUNT(e) - COUNT(DISTINCT CONCAT(CAST(e.paciente.id AS string), e.tipoExame))
        FROM Exame e
        WHERE e.dataRealizacao >= :dataInicio
        """)
    Long contarExamesRepetidos(@Param("dataInicio") LocalDate dataInicio);

    @Query("""
        SELECT COUNT(e)
        FROM Exame e
        WHERE e.paciente.id = :pacienteId
        AND e.tipoExame = :tipoExame
        AND e.dataRealizacao >= :dataInicio
        """)
    long contarPorPacienteTipoEPeriodo(
            @Param("pacienteId") Long pacienteId,
            @Param("tipoExame") TipoExame tipoExame,
            @Param("dataInicio") LocalDate dataInicio
    );
}
