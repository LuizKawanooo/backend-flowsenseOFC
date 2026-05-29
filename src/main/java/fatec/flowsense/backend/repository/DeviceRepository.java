package fatec.flowsense.backend.repository;

import fatec.flowsense.backend.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findBySensorId(Long sensorId);

    Optional<Device> findBySensorIdAndOwnerEmail(Long sensorId, String ownerEmail);

    Optional<Device> findByIdAndOwnerEmail(Long id, String ownerEmail);

    Optional<Device> findByDeviceToken(String deviceToken);

    List<Device> findByOwnerEmail(String ownerEmail);
}	