package fatec.flowsense.backend.controller;

import fatec.flowsense.backend.controller.dto.SensorDisponivelDto;
import fatec.flowsense.backend.controller.dto.SensorFluxoDto;
import fatec.flowsense.backend.entities.Device;
import fatec.flowsense.backend.repository.DeviceRepository;
import fatec.flowsense.backend.service.FluxoTempoRealService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sensores")
@CrossOrigin(origins = "*")
public class SensorController {

    private final FluxoTempoRealService fluxoTempoRealService;
    private final DeviceRepository deviceRepository;

    public SensorController(
            FluxoTempoRealService fluxoTempoRealService,
            DeviceRepository deviceRepository
    ) {
        this.fluxoTempoRealService = fluxoTempoRealService;
        this.deviceRepository = deviceRepository;
    }

    @PostMapping("/fluxo")
    public ResponseEntity<?> receberFluxo(
            @RequestHeader("X-Device-Token") String deviceToken,
            @RequestBody SensorFluxoDto dto
    ) {
        Device device = deviceRepository
                .findByDeviceToken(deviceToken)
                .orElse(null);

        if (device == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "erro", "Token do dispositivo inválido"
            ));
        }

        if (!device.getSensorId().equals(dto.getSensorId())) {
            return ResponseEntity.status(403).body(Map.of(
                    "erro", "Sensor ID não pertence a este token"
            ));
        }

        fluxoTempoRealService.receberFluxo(dto, device);

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mensagem", "Fluxo recebido",
                "sensorId", dto.getSensorId()
        ));
    }

    @GetMapping("/disponiveis")
    public List<SensorDisponivelDto> listarSensoresDisponiveis(Authentication authentication) {

        String ownerEmail = getEmail(authentication);

        System.out.println("Buscando sensores do usuário: " + ownerEmail);

        return fluxoTempoRealService.listarSensoresDisponiveis(ownerEmail);
    }

    @GetMapping("/{sensorId}/atual")
    public ResponseEntity<?> buscarDadosAtuais(
            @PathVariable Long sensorId,
            Authentication authentication
    ) {
        String ownerEmail = getEmail(authentication);

        boolean pertenceAoUsuario = deviceRepository
                .findBySensorId(sensorId)
                .map(device -> device.getOwnerEmail().equals(ownerEmail))
                .orElse(false);

        if (!pertenceAoUsuario) {
            return ResponseEntity.status(403).body(Map.of(
                    "erro", "Este sensor não pertence à sua conta"
            ));
        }

        return ResponseEntity.ok(
                fluxoTempoRealService.buscarDadosAtuais(sensorId)
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