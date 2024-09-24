package services.socialBotManagerService.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "models")
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private byte[] botModel;

    public Model() {
    }

    // public Model (String name, byte[] botModel) {
    //     this.name = name;
    //     this.botModel = botModel;
    // }
    
    public Model(UUID id, String name, byte[] botModel) {
        this.id = id;
        this.name = name;
        this.botModel = botModel;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getModel() {
        return botModel;
    }

    public void setModel(byte[] botModel) {
        this.botModel = botModel;
    }
    
}
