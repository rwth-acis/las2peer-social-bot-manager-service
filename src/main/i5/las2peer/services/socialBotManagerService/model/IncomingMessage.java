package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class IncomingMessage {
	String intentKeyword;
	String entityKeyword;
    String NluID;

	ArrayList<String> responses;

	// Intent keywords used as keys
	HashMap<String, IncomingMessage> followupMessages;

	String triggeredFunctionId;

	public IncomingMessage(String intent, String NluID) {
		this.intentKeyword = intent;
		this.followupMessages = new HashMap<String, IncomingMessage>();
		this.responses = new ArrayList<String>();
        if(NluID == ""){
            this.NluID = "0";
        } else this.NluID = NluID;
	}

	public String getIntentKeyword() {
		return intentKeyword;
	}

	public String getEntityKeyword() {
		return entityKeyword;
	}

	public void setEntityKeyword(String entityKeyword) {
		this.entityKeyword = entityKeyword;
	}
    
	public String getNluID() {
		return NluID;
	}
 
	public HashMap<String, IncomingMessage> getFollowingMessages() {
		return followupMessages;
	}

	public void addFollowupMessage(String intentKeyword, IncomingMessage msg) {
		this.followupMessages.put(intentKeyword, msg);
	}

	public void addResponse(String response) {
		this.responses.add(response);
	}

	public String getResponse(Random random) {
		if (responses.isEmpty()) {
			return null;
		} else {
			return responses.get(random.nextInt(responses.size()));
		}
	}

	public void setTriggeredFunction(ServiceFunction triggeredFunction) {
		this.triggeredFunctionId = triggeredFunction.getId();
	}

	public String getTriggeredFunctionId() {
		return this.triggeredFunctionId;
	}
}
