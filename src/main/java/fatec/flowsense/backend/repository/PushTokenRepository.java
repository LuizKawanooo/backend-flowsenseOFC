package fatec.flowsense.backend.repository;

import fatec.flowsense.backend.entities.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    List<PushToken> findByOwnerEmail(String ownerEmail);

    Optional<PushToken> findByToken(String token);
}