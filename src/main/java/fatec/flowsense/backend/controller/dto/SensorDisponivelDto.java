package fatec.flowsense.backend.controller.dto;

public class SensorDisponivelDto {

    private String id;
    private String name;
    private String ip;
    private boolean online;

    public SensorDisponivelDto() {
    }

    public SensorDisponivelDto(String id, String name, String ip, boolean online) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.online = online;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public boolean isOnline() {
        return online;
    }
}