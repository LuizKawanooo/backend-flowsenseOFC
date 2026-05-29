package fatec.flowsense.backend.controller.dto;

import java.time.LocalDate;

public class ConsumoDiaDto {

    private LocalDate dataConsumo;
    private int dia;
    private double litros;

    public ConsumoDiaDto(LocalDate dataConsumo, int dia, double litros) {
        this.dataConsumo = dataConsumo;
        this.dia = dia;
        this.litros = litros;
    }

    public LocalDate getDataConsumo() {
        return dataConsumo;
    }

    public int getDia() {
        return dia;
    }

    public double getLitros() {
        return litros;
    }
}