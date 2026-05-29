package fatec.flowsense.backend.controller;

import fatec.flowsense.backend.service.FluxoTempoRealService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/sensores/fluxo")
@CrossOrigin(origins = "*")
public class FluxoTempoRealController {

    private final FluxoTempoRealService fluxoTempoRealService;

    public FluxoTempoRealController(FluxoTempoRealService fluxoTempoRealService) {
        this.fluxoTempoRealService = fluxoTempoRealService;
    }

    @GetMapping("/atual")
    public Map<String, Object> fluxoAtual() {
        return fluxoTempoRealService.buscarDadosAtuais(1L);
    }
}