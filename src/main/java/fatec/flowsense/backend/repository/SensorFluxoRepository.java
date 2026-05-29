package fatec.flowsense.backend.repository;

import fatec.flowsense.backend.entities.SensorFluxo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorFluxoRepository extends JpaRepository<SensorFluxo, Long> {

	Optional<SensorFluxo> findTopByOrderByIdDesc();
}