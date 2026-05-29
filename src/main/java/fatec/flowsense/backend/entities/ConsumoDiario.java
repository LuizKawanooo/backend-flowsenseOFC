package fatec.flowsense.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_consumo_diario")
public class ConsumoDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sensorId;

    private String nomeSensor;

    private LocalDate dataConsumo;

    private Double totalLitros;

    private LocalDateTime atualizadoEm;

    @Column(name = "owner_email")
    private String ownerEmail;

    public ConsumoDiario() {
    }

    public ConsumoDiario(
            Long sensorId,
            String nomeSensor,
            LocalDate dataConsumo,
            Double totalLitros,
            LocalDateTime atualizadoEm
    ) {
        this.sensorId = sensorId;
        this.nomeSensor = nomeSensor;
        this.dataConsumo = dataConsumo;
        this.totalLitros = totalLitros;
        this.atualizadoEm = atualizadoEm;
    }

    public ConsumoDiario(
            Long sensorId,
            String nomeSensor,
            String ownerEmail,
            LocalDate dataConsumo,
            Double totalLitros,
            LocalDateTime atualizadoEm
    ) {
        this.sensorId = sensorId;
        this.nomeSensor = nomeSensor;
        this.ownerEmail = ownerEmail;
        this.dataConsumo = dataConsumo;
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

    public String getNomeSensor() {
        return nomeSensor;
    }

    public void setNomeSensor(String nomeSensor) {
        this.nomeSensor = nomeSensor;
    }

    public LocalDate getDataConsumo() {
        return dataConsumo;
    }

    public void setDataConsumo(LocalDate dataConsumo) {
        this.dataConsumo = dataConsumo;
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

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}