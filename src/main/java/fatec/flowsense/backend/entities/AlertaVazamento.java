package fatec.flowsense.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_alertas_vazamento")
public class AlertaVazamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sensorId;

    private String nomeSensor;

    @Column(name = "owner_email")
    private String ownerEmail;

    private Double fluxoDetectado;

    private Boolean lido = false;

    private LocalDateTime criadoEm;

    public AlertaVazamento() {
    }

    public AlertaVazamento(
            Long sensorId,
            String nomeSensor,
            String ownerEmail,
            Double fluxoDetectado
    ) {
        this.sensorId = sensorId;
        this.nomeSensor = nomeSensor;
        this.ownerEmail = ownerEmail;
        this.fluxoDetectado = fluxoDetectado;
        this.lido = false;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public String getNomeSensor() {
        return nomeSensor;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public Double getFluxoDetectado() {
        return fluxoDetectado;
    }

    public Boolean getLido() {
        return lido;
    }

    public void setLido(Boolean lido) {
        this.lido = lido;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}