package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ChatResponse {
	String response;
	String triggeredFunctionId;
	HashMap<IncomingMessage,String> triggerEntity;
	String fileURL;
	String errorMessage;
	String type;
	public ChatResponse(String response, String fileURL, String errorMessage, String type) {
		this.response = response;
		this.fileURL = fileURL;
		this.errorMessage  = errorMessage;
		this.triggerEntity = new HashMap<IncomingMessage,String>();
		this.type = type;
		
	}
    public String getResponse(){
        return this.response;
    }
    
	public String getFileURL() {
		return fileURL;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
    
    public void setTriggeredFunctionId(String functionId){
        this.triggeredFunctionId = functionId;
    }
    
    
    public String getTriggeredFunctionId(){
        return this.triggeredFunctionId;
    }
    
    public String getTriggerEntity(IncomingMessage m){
        return this.triggerEntity.get(m);
    }
    
    public void addTriggerEntity(IncomingMessage m,String triggerEntity){
        this.triggerEntity.put(m, triggerEntity);
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}