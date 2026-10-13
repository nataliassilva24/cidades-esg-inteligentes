package br.com.fiap.healthtrack.domain.repository;

import br.com.fiap.healthtrack.domain.entity.Prontuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {

    Page<Prontuario> findByPacienteId(Long pacienteId, Pageable pageable);
}
