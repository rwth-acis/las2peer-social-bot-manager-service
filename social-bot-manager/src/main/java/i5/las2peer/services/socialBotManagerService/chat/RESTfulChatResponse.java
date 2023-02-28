package i5.las2peer.services.socialBotManagerService.chat;

public class RESTfulChatResponse {
    private String message;
    
    public RESTfulChatResponse() {
    }

    public RESTfulChatResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
