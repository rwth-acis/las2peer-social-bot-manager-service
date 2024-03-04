package i5.las2peer.services.socialBotManagerService.chat;

public class InteractiveChatElement {
    private String intent;
    private String label;
    private boolean isFile;
    private boolean rateable; 
    private boolean async;

    public InteractiveChatElement(String intent, String label) {
        this(intent, label, false, false, false);
    }

    public InteractiveChatElement(String intent, String label, boolean isFile, boolean rateable, boolean async){
        this.intent = intent;
        this.label = label;
        this.isFile = isFile;
        this.rateable = rateable;
        this.async = async;
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

    public boolean isRateable(){
        return rateable;
    }

    public void setRateable(boolean rateable){
        this.rateable = rateable;
    }

    public boolean isAsync(){
        return async;
    }

    public void setAsync(boolean async){
        this.async = async;
    }
}
