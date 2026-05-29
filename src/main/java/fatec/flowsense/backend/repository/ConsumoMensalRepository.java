package fatec.flowsense.backend.repository;

import fatec.flowsense.backend.entities.ConsumoMensal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsumoMensalRepository extends JpaRepository<ConsumoMensal, Long> {

    Optional<ConsumoMensal> findBySensorIdAndAnoMes(Long sensorId, String anoMes);
}