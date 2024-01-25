package i5.las2peer.services.socialBotManagerService.chat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;
import net.minidev.json.JSONObject;

public class RESTfulChatResponse {
    private String message;
    private List<Object> interactiveElements;
    private String fileId;
    private InteractiveChatElementType type;
    private JSONObject reqBody;
    private boolean isFile;
    private boolean rateable; 
    private boolean asynchron;

    public RESTfulChatResponse(String text, HashMap<String, IncomingMessage> hashMap, String type, IncomingMessage currentMessage) {
        this(text);
        reqBody = new JSONObject();
        HashSet<InteractiveChatElement> icel = new HashSet<InteractiveChatElement>();
        setType(type);
        isFile = false;
        asynchron = false;
        if(hashMap != null){
            for (Entry<String, IncomingMessage> entry : hashMap.entrySet()) {
                String key = entry.getKey();
                IncomingMessage value = entry.getValue();
                String intent = key;
                if(intent==null||intent=="") intent = value.getIntentKeyword();
                InteractiveChatElement ice = new InteractiveChatElement(intent, value.getIntentLabel(), value.expectsFile(), value.isRateable() ,value.getAsynchron());
                icel.add(ice);    
                if(entry.getValue().expectsFile()){
                    isFile = true;
                }
                if(entry.getValue().isRateable()){
                    rateable = true;
                }
                if(entry.getValue().getAsynchron()){
                    asynchron = true;
                }
            }
        }
        if (currentMessage != null) {
            if(currentMessage.isRateable()){
                rateable = true; 
            }
            if(currentMessage.getAsynchron()) {
                asynchron = true;
            }
        }
        System.out.println("Current Message is null.");
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

    public InteractiveChatElementType getType() {
        return type;
    }

    public void setType(String type) {
        this.type = InteractiveChatElementType.valueOf(type.toUpperCase());
    }

    public JSONObject getReqBody() {
        return reqBody;
    }

    public void setReqBody(JSONObject reqBody) {
        this.reqBody = reqBody;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }

    public boolean isRateable() {
        return rateable;
    }

    public void setRateable(boolean rateable) {
        this.rateable = rateable;
    }

    public boolean asynchron() {
        return asynchron;
    }

    public void setAsynchron(boolean asynchron) {
        this.asynchron = asynchron;
    }

}
