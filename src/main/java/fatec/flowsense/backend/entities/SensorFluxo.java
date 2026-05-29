package fatec.flowsense.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_sensor_fluxo")
public class SensorFluxo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sensorId;

    private Double fluxo;

    private LocalDateTime dataHora;

    public SensorFluxo() {
    }

    public SensorFluxo(Long sensorId, Double fluxo, LocalDateTime dataHora) {
        this.sensorId = sensorId;
        this.fluxo = fluxo;
        this.dataHora = dataHora;
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

    public Double getFluxo() {
        return fluxo;
    }

    public void setFluxo(Double fluxo) {
        this.fluxo = fluxo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}