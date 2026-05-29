package fatec.flowsense.backend.repository;

import fatec.flowsense.backend.entities.ConsumoDiario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConsumoDiarioRepository extends JpaRepository<ConsumoDiario, Long> {

    Optional<ConsumoDiario> findBySensorIdAndDataConsumo(
            Long sensorId,
            LocalDate dataConsumo
    );

    Optional<ConsumoDiario> findBySensorIdAndOwnerEmailAndDataConsumo(
            Long sensorId,
            String ownerEmail,
            LocalDate dataConsumo
    );

    List<ConsumoDiario> findBySensorIdOrderByDataConsumoDesc(
            Long sensorId
    );

    List<ConsumoDiario> findBySensorIdAndOwnerEmailAndDataConsumoBetweenOrderByDataConsumoAsc(
            Long sensorId,
            String ownerEmail,
            LocalDate inicio,
            LocalDate fim
    );

    @Query("""
        SELECT COALESCE(SUM(c.totalLitros), 0)
        FROM ConsumoDiario c
        WHERE c.sensorId = :sensorId
        AND c.ownerEmail = :ownerEmail
        AND c.dataConsumo BETWEEN :inicio AND :fim
    """)
    Double somarConsumoMensal(
            @Param("sensorId") Long sensorId,
            @Param("ownerEmail") String ownerEmail,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}