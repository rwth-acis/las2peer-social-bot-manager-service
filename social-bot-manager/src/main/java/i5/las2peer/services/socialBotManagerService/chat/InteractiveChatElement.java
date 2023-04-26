package i5.las2peer.services.socialBotManagerService.chat;

public class InteractiveChatElement {
    private String intent;
    private String label;

    public InteractiveChatElement(String intent, String label){
        this.intent = intent;
        this.label = label;
    }

    public String getIntent() {
        return intent;
    }
    public void setIntent(String intent) {
        this.intent = intent;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
}
