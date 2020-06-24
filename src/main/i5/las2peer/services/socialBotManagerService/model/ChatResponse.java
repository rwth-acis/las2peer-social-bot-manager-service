package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ChatResponse {
	String response;
	String triggeredFunctionId;

	public ChatResponse(String response) {
		this.response = response;
		
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

}