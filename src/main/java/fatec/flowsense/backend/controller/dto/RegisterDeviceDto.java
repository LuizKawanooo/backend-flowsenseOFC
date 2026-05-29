package fatec.flowsense.backend.controller.dto;

public class RegisterDeviceDto {

    private Long sensorId;
    private String name;

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}