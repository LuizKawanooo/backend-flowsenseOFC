package fatec.flowsense.backend.controller.dto;

public class SensorFluxoDto {

    private Long sensorId;
    private String nome;
    private Double fluxo;
    private Double litrosSegundo;
    private Long pulsos;
    private Double intervalo;

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getFluxo() {
        return fluxo;
    }

    public void setFluxo(Double fluxo) {
        this.fluxo = fluxo;
    }

    public Double getLitrosSegundo() {
        return litrosSegundo;
    }

    public void setLitrosSegundo(Double litrosSegundo) {
        this.litrosSegundo = litrosSegundo;
    }

    public Long getPulsos() {
        return pulsos;
    }

    public void setPulsos(Long pulsos) {
        this.pulsos = pulsos;
    }

    public Double getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(Double intervalo) {
        this.intervalo = intervalo;
    }
}