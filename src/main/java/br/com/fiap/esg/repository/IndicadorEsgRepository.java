package br.com.fiap.esg.repository;

import br.com.fiap.esg.model.IndicadorEsg;
import br.com.fiap.esg.model.Pilar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndicadorEsgRepository extends JpaRepository<IndicadorEsg, Long> {

    List<IndicadorEsg> findByCidadeId(Long cidadeId);

    List<IndicadorEsg> findByCidadeIdAndPilar(Long cidadeId, Pilar pilar);
}
