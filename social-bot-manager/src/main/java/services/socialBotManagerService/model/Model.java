package services.socialBotManagerService.model;

import jakarta.persistence.*;

@Entity
@Table(name = "models")
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private byte[] botModel;

    public Model() {
    }

    public Model (String name, byte[] botModel) {
        this.name = name;
        this.botModel = botModel;
    }
    
    public Model(Long id, String name, byte[] botModel) {
        this.id = id;
        this.name = name;
        this.botModel = botModel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
