package i5.las2peer.services.socialBotManagerService.chat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;

public class RESTfulChatResponse {
    private String message;
    private List<Object> interactiveElements;
    private String fileId;

    public RESTfulChatResponse(String text, HashMap<String, IncomingMessage> hashMap) {
        this(text);
        HashSet<InteractiveChatElement> icel = new HashSet<InteractiveChatElement>();
        for (Entry<String, IncomingMessage> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            IncomingMessage value = entry.getValue();
            String intent = key;
            if(intent==null||intent=="") intent = value.getIntentKeyword();
            InteractiveChatElement ice = new InteractiveChatElement(intent, value.getFollowupMessageType(), value.getIntentLabel());
            icel.add(ice);
        }
        interactiveElements = Arrays.asList(icel.toArray());
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

    public List<Object> getInteractiveElements() {
        return interactiveElements;
    }

    public void setInteractiveElements(List<Object> interactiveElements) {
        this.interactiveElements = interactiveElements;
    }
    
    public String getFileId() {
        return fileId;
    }

    public void setFileID(String fileId) {
        this.fileId = fileId;
    }

}
