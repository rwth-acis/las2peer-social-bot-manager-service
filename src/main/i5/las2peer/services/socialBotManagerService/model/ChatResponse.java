package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ChatResponse {
	String response;
	String triggeredFunctionId;
	String triggerEntity;
	String fileURL;
	String errorMessage;
	public ChatResponse(String response, String fileURL, String errorMessage) {
		this.response = response;
		this.fileURL = fileURL;
		this.errorMessage  = errorMessage;
		this.triggerEntity = "";
		
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
    
    public String getTriggerEntity(){
        return this.triggerEntity;
    }
    
    public void addTriggerEntity(String triggerEntity){
        this.triggerEntity = triggerEntity;
    }

}