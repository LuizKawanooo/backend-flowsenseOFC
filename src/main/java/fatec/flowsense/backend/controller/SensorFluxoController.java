package fatec.flowsense.backend.controller;

import fatec.flowsense.backend.entities.SensorFluxo;
import fatec.flowsense.backend.repository.SensorFluxoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sensores/fluxo")
public class SensorFluxoController {

    private final SensorFluxoRepository sensorFluxoRepository;

    public SensorFluxoController(SensorFluxoRepository sensorFluxoRepository) {
        this.sensorFluxoRepository = sensorFluxoRepository;
    }

    @GetMapping
    public List<SensorFluxo> listarTodos() {
        return sensorFluxoRepository.findAll();
    }

    @GetMapping("/ultimo")
    public SensorFluxo buscarUltimo() {
        return sensorFluxoRepository
                .findTopByOrderByIdDesc()
                .orElse(null);
    }
}