package fatec.flowsense.backend.controller;

import fatec.flowsense.backend.controller.dto.PushTokenDto;
import fatec.flowsense.backend.entities.PushToken;
import fatec.flowsense.backend.repository.PushTokenRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/push-token")
@CrossOrigin(origins = "*")
public class PushTokenController {

    private final PushTokenRepository pushTokenRepository;

    public PushTokenController(PushTokenRepository pushTokenRepository) {
        this.pushTokenRepository = pushTokenRepository;
    }

    @PostMapping
    public ResponseEntity<?> salvarToken(
            @RequestBody PushTokenDto dto,
            Authentication authentication
    ) {
        String ownerEmail = getEmail(authentication);

        System.out.println("=================================");
        System.out.println("SALVANDO TOKEN PUSH");
        System.out.println("Owner email: " + ownerEmail);
        System.out.println("Token recebido: " + dto.getToken());
        System.out.println("Platform: " + dto.getPlatform());
        System.out.println("=================================");

        if (dto.getToken() == null || dto.getToken().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", "Token é obrigatório"
            ));
        }

        PushToken pushToken = pushTokenRepository
                .findByToken(dto.getToken())
                .orElse(new PushToken());

        pushToken.setOwnerEmail(ownerEmail);
        pushToken.setToken(dto.getToken());
        pushToken.setPlatform(dto.getPlatform() != null ? dto.getPlatform() : "android");
        pushToken.setCreatedAt(LocalDateTime.now());

        pushTokenRepository.save(pushToken);

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mensagem", "Token push salvo",
                "ownerEmail", ownerEmail
        ));
    }

    private String getEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");

            if (email != null && !email.isBlank()) {
                return email;
            }

            return jwt.getClaimAsString("sub");
        }

        return authentication.getName();
    }
}