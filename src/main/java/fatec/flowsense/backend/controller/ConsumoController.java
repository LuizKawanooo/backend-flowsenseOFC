package fatec.flowsense.backend.controller;

import fatec.flowsense.backend.controller.dto.ConsumoDiaDto;
import fatec.flowsense.backend.service.ConsumoService;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consumo")
@CrossOrigin(origins = "*")
public class ConsumoController {

    private final ConsumoService consumoService;

    public ConsumoController(ConsumoService consumoService) {
        this.consumoService = consumoService;
    }

    @GetMapping("/{sensorId}/mensal")
    public Map<String, Object> consumoMensal(
            @PathVariable Long sensorId,
            Authentication authentication
    ) {
        String ownerEmail = getEmail(authentication);

        double totalMensal = consumoService.calcularConsumoMensal(sensorId, ownerEmail);

        return Map.of(
                "sensorId", sensorId,
                "monthlyTotal", totalMensal
        );
    }

    @GetMapping("/{sensorId}/mensal/grafico")
    public Map<String, Object> graficoConsumoMensal(
            @PathVariable Long sensorId,
            Authentication authentication
    ) {
        String ownerEmail = getEmail(authentication);

        List<ConsumoDiaDto> dias = consumoService.buscarGraficoMensal(sensorId, ownerEmail);

        double total = dias.stream()
                .mapToDouble(ConsumoDiaDto::getLitros)
                .sum();

        return Map.of(
                "sensorId", sensorId,
                "monthlyTotal", total,
                "dias", dias
        );
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