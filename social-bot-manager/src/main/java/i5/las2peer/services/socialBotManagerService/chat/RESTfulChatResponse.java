package i5.las2peer.services.socialBotManagerService.chat;

import java.util.List;

public class RESTfulChatResponse {
    private String message;
    private List<InteractiveChatElement> interactiveElements;

    public RESTfulChatResponse() {
    }

    public RESTfulChatResponse(String message) {
        this.message = message;
    }

    public RESTfulChatResponse(String message, List<InteractiveChatElement> interactiveElements) {
        this.message = message;
        this.interactiveElements = interactiveElements;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<InteractiveChatElement> getInteractiveElements() {
        return interactiveElements;
    }

    public void setInteractiveElements(List<InteractiveChatElement> interactiveElements) {
        this.interactiveElements = interactiveElements;
    }
    
}
