package fatec.flowsense.backend.controller;

import fatec.flowsense.backend.controller.dto.RegisterDeviceDto;

import fatec.flowsense.backend.entities.Device;
import fatec.flowsense.backend.repository.DeviceRepository;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/devices")
@CrossOrigin(origins = "*")
public class DeviceController {

    private final DeviceRepository deviceRepository;

    public DeviceController(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarDispositivo(
            @RequestBody RegisterDeviceDto dto,
            Authentication authentication
    ) {
        String ownerEmail = getEmail(authentication);

        if (dto.getSensorId() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", "sensorId é obrigatório"
            ));
        }

        Device deviceExistente = deviceRepository
                .findBySensorId(dto.getSensorId())
                .orElse(null);

        if (deviceExistente != null) {

            if (!deviceExistente.getOwnerEmail().equals(ownerEmail)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "erro", "Este dispositivo já está vinculado a outra conta"
                ));
            }

            deviceExistente.setActive(true);
            deviceExistente.setResetRequested(false);

            if (dto.getName() != null && !dto.getName().isBlank()) {
                deviceExistente.setName(dto.getName());
            }

            deviceRepository.save(deviceExistente);

            return ResponseEntity.ok(Map.of(
                    "sensorId", deviceExistente.getSensorId(),
                    "name", deviceExistente.getName(),
                    "deviceToken", deviceExistente.getDeviceToken(),
                    "ownerEmail", deviceExistente.getOwnerEmail()
            ));
        }

        String token = UUID.randomUUID().toString();

        String name = dto.getName();

        if (name == null || name.isBlank()) {
            name = "Sensor " + dto.getSensorId();
        }

        Device novoDevice = new Device(
                dto.getSensorId(),
                name,
                ownerEmail,
                token
        );

        novoDevice.setActive(true);
        novoDevice.setResetRequested(false);

        deviceRepository.save(novoDevice);

        return ResponseEntity.ok(Map.of(
                "sensorId", novoDevice.getSensorId(),
                "name", novoDevice.getName(),
                "deviceToken", novoDevice.getDeviceToken(),
                "ownerEmail", novoDevice.getOwnerEmail()
        ));
    }

    @GetMapping("/meus")
    public List<Device> meusDispositivos(Authentication authentication) {
        String ownerEmail = getEmail(authentication);

        return deviceRepository.findByOwnerEmail(ownerEmail);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluirDispositivo(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String ownerEmail = getEmail(authentication);

        Device device = deviceRepository
                .findBySensorIdAndOwnerEmail(id, ownerEmail)
                .orElse(null);

        if (device == null) {
            device = deviceRepository
                    .findByIdAndOwnerEmail(id, ownerEmail)
                    .orElse(null);
        }

        if (device == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "erro", "Dispositivo não encontrado nesta conta",
                    "idRecebido", id,
                    "ownerEmail", ownerEmail
            ));
        }

        Long idBanco = device.getId();
        Long sensorId = device.getSensorId();
        String nome = device.getName();

        deviceRepository.delete(device);

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mensagem", "Dispositivo excluído definitivamente do banco",
                "idBanco", idBanco,
                "sensorId", sensorId,
                "name", nome
        ));
    }

    @GetMapping("/comando")
    public ResponseEntity<?> buscarComandoDoDispositivo(
            @RequestHeader("X-Device-Token") String deviceToken
    ) {
        Device device = deviceRepository
                .findByDeviceToken(deviceToken)
                .orElse(null);

        if (device == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "erro", "Token inválido"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "reset", Boolean.TRUE.equals(device.getResetRequested()),
                "sensorId", device.getSensorId()
        ));
    }

    @PostMapping("/reset-confirmado")
    public ResponseEntity<?> confirmarReset(
            @RequestHeader("X-Device-Token") String deviceToken
    ) {
        Device device = deviceRepository
                .findByDeviceToken(deviceToken)
                .orElse(null);

        if (device == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "erro", "Token inválido"
            ));
        }

        deviceRepository.delete(device);

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mensagem", "Reset confirmado e dispositivo removido definitivamente"
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