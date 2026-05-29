package fatec.flowsense.backend.controller;

import fatec.flowsense.backend.entities.AlertaVazamento;
import fatec.flowsense.backend.repository.AlertaVazamentoRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alertas/vazamento")
@CrossOrigin(origins = "*")
public class AlertaVazamentoController {

    private final AlertaVazamentoRepository alertaVazamentoRepository;

    public AlertaVazamentoController(AlertaVazamentoRepository alertaVazamentoRepository) {
        this.alertaVazamentoRepository = alertaVazamentoRepository;
    }

    @GetMapping("/pendentes")
    public List<AlertaVazamento> listarPendentes(Authentication authentication) {
        String ownerEmail = getEmail(authentication);

        return alertaVazamentoRepository
                .findByOwnerEmailAndLidoFalseOrderByCriadoEmDesc(ownerEmail);
    }

    @PostMapping("/{id}/lido")
    public ResponseEntity<?> marcarComoLido(@PathVariable Long id) {
        AlertaVazamento alerta = alertaVazamentoRepository
                .findById(id)
                .orElse(null);

        if (alerta == null) {
            return ResponseEntity.notFound().build();
        }

        alerta.setLido(true);
        alertaVazamentoRepository.save(alerta);

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mensagem", "Alerta marcado como lido"
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