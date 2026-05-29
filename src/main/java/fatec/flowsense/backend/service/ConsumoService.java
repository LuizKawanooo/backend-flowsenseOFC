package fatec.flowsense.backend.service;

import fatec.flowsense.backend.controller.dto.ConsumoDiaDto;
import fatec.flowsense.backend.entities.ConsumoDiario;
import fatec.flowsense.backend.repository.ConsumoDiarioRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsumoService {

    private final ConsumoDiarioRepository consumoDiarioRepository;

    public ConsumoService(ConsumoDiarioRepository consumoDiarioRepository) {
        this.consumoDiarioRepository = consumoDiarioRepository;
    }

    public double calcularConsumoMensal(Long sensorId, String ownerEmail) {

        LocalDate hoje = LocalDate.now();

        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        Double total = consumoDiarioRepository.somarConsumoMensal(
                sensorId,
                ownerEmail,
                inicioMes,
                fimMes
        );

        return total != null ? total : 0.0;
    }

    public List<ConsumoDiaDto> buscarGraficoMensal(Long sensorId, String ownerEmail) {

        LocalDate hoje = LocalDate.now();

        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        List<ConsumoDiario> registros =
                consumoDiarioRepository.findBySensorIdAndOwnerEmailAndDataConsumoBetweenOrderByDataConsumoAsc(
                        sensorId,
                        ownerEmail,
                        inicioMes,
                        fimMes
                );

        Map<LocalDate, Double> consumoPorData = registros.stream()
                .collect(Collectors.toMap(
                        ConsumoDiario::getDataConsumo,
                        consumo -> consumo.getTotalLitros() != null ? consumo.getTotalLitros() : 0.0
                ));

        List<ConsumoDiaDto> resultado = new ArrayList<>();

        LocalDate data = inicioMes;

        while (!data.isAfter(fimMes)) {

            double litros = consumoPorData.getOrDefault(data, 0.0);

            resultado.add(new ConsumoDiaDto(
                    data,
                    data.getDayOfMonth(),
                    litros
            ));

            data = data.plusDays(1);
        }

        return resultado;
    }
}