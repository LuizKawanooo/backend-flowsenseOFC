package fatec.flowsense.backend.controller;

import fatec.flowsense.backend.service.PushNotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/push-teste")
@CrossOrigin(origins = "*")
public class PushTestController {

    private final PushNotificationService pushNotificationService;

    public PushTestController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @PostMapping
    public ResponseEntity<?> testarPush(Authentication authentication) {
        String ownerEmail = getEmail(authentication);

        pushNotificationService.enviarAlertaVazamento(
                ownerEmail,
                999L,
                "Teste de vazamento",
                250.0
        );

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mensagem", "Teste de push executado",
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