package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ChatResponse {
	String response;
	String triggeredFunctionId;
	String triggerEntity;
	public ChatResponse(String response) {
		this.response = response;
		this.triggerEntity = "";
		
	}
    public String getResponse(){
        return this.response;
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