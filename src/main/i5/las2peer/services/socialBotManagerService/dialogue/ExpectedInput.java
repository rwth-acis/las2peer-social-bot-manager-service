package i5.las2peer.services.socialBotManagerService.dialogue;

public class ExpectedInput {
    
    InputType type;
    String intend;
    String entity;
    
    protected InputType getType() {
        return type;
    }
    protected void setType(InputType type) {
        this.type = type;
    }
    protected String getIntend() {
        return intend;
    }
    protected void setIntend(String intend) {
        this.intend = intend;
    }
    public ExpectedInput(InputType type, String intend) {
	super();
	this.type = type;
	this.intend = intend;
    }
    public String getEntity() {
        return entity;
    }
    public void setEntity(String entity) {
        this.entity = entity;
    }
    @Override
    public String toString() {
	return "ExpectedInput [type=" + type + ", intend=" + intend + ", entity=" + entity + "]";
    }
    
    
}
