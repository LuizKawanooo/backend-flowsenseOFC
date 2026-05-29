package fatec.flowsense.backend.repository;

import fatec.flowsense.backend.entities.AlertaVazamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaVazamentoRepository extends JpaRepository<AlertaVazamento, Long> {

    List<AlertaVazamento> findByOwnerEmailAndLidoFalseOrderByCriadoEmDesc(String ownerEmail);
}