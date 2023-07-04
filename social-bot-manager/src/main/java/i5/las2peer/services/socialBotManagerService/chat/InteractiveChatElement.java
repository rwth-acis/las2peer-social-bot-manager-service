package i5.las2peer.services.socialBotManagerService.chat;

public class InteractiveChatElement {
    private String intent;
    private String label;
    private boolean isFile; 

    public InteractiveChatElement(String intent, String label, boolean isFile){
        this.intent = intent;
        this.label = label;
        this.isFile = isFile;
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

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }
}
