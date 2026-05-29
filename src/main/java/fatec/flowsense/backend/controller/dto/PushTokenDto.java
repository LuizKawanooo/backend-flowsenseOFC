package fatec.flowsense.backend.controller.dto;

public class PushTokenDto {

    private String token;
    private String platform;

    public String getToken() {
        return token;
    }

    public String getPlatform() {
        return platform;
    }
}