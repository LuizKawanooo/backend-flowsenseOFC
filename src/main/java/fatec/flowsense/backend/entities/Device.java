package fatec.flowsense.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sensorId;

    private String name;

    private String ownerEmail;

    @Column(unique = true)
    private String deviceToken;

    private LocalDateTime createdAt;
    
    private Boolean active = true;

    private Boolean resetRequested = false;

    public Device() {
    }

    public Device(Long sensorId, String name, String ownerEmail, String deviceToken) {
        this.sensorId = sensorId;
        this.name = name;
        this.ownerEmail = ownerEmail;
        this.deviceToken = deviceToken;
        this.createdAt = LocalDateTime.now();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getResetRequested() {
        return resetRequested;
    }

    public void setResetRequested(Boolean resetRequested) {
        this.resetRequested = resetRequested;
    }
}