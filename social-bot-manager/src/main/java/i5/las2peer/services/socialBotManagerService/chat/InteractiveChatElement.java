package i5.las2peer.services.socialBotManagerService.chat;

public class InteractiveChatElement {
    private String intent;
    private InteractiveChatElementType type;
    private String description;

    public InteractiveChatElement(String intent, String type, String description){
        this.intent = intent;
        this.type = InteractiveChatElementType.valueOf(type);
        this.description = description;
    }

    public String getIntent() {
        return intent;
    }
    public void setIntent(String intent) {
        this.intent = intent;
    }
    public InteractiveChatElementType getType() {
        return type;
    }
    public void setType(InteractiveChatElementType type) {
        this.type = type;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
