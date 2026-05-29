package fatec.flowsense.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_consumo_mensal")
public class ConsumoMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sensorId;

    private String anoMes;

    private Double totalLitros;

    private LocalDateTime atualizadoEm;

    public ConsumoMensal() {
    }

    public ConsumoMensal(Long sensorId, String anoMes, Double totalLitros, LocalDateTime atualizadoEm) {
        this.sensorId = sensorId;
        this.anoMes = anoMes;
        this.totalLitros = totalLitros;
        this.atualizadoEm = atualizadoEm;
    }

    public Long getId() {
        return id;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public String getAnoMes() {
        return anoMes;
    }

    public void setAnoMes(String anoMes) {
        this.anoMes = anoMes;
    }

    public Double getTotalLitros() {
        return totalLitros;
    }

    public void setTotalLitros(Double totalLitros) {
        this.totalLitros = totalLitros;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}