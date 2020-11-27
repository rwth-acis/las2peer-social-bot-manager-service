package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import i5.las2peer.services.socialBotManagerService.model.ChatResponse;

public class IncomingMessage {
	String intentKeyword;
	String entityKeyword;
    String NluID;
    Boolean containsFile;

	ArrayList<ChatResponse> responses;

	// Intent keywords used as keys
	HashMap<String, IncomingMessage> followupMessages;

	String triggeredFunctionId;

	private static String[][] UMLAUT_REPLACEMENTS = { { new String("Ä"), "Ae" }, { new String("Ü"), "Ue" },
			{ new String("Ö"), "Oe" }, { new String("ä"), "ae" }, { new String("ü"), "ue" }, { new String("ö"), "oe" },
			{ new String("ß"), "ss" } };

	public static String replaceUmlaute(String orig) {
		String result = orig;

		for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
			result = result.replace(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
		}

		return result;
	}


	public IncomingMessage(String intent, String NluID, Boolean containsFile) {
		if(intent != "") {
			this.intentKeyword = replaceUmlaute(intent);
		} else intent = "";
		this.followupMessages = new HashMap<String, IncomingMessage>();
		this.responses = new ArrayList<ChatResponse>();
		this.containsFile = containsFile;
        if(NluID == ""){
            this.NluID = "";
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
		this.followupMessages.put(replaceUmlaute(intentKeyword), msg);
	}

	public void addResponse(ChatResponse response) {
		this.responses.add(response);
	}

	public ChatResponse getResponse(Random random) {
		if (responses.isEmpty()) {
			return null;
		} else {
			return responses.get(random.nextInt(responses.size()));
		}
	}
	
	public ArrayList<ChatResponse> getResponseArray() {
		if (responses.isEmpty()) {
			return null;
		} else {
			return responses;
		}
	}

	public void setTriggeredFunction(ServiceFunction triggeredFunction) {
		this.triggeredFunctionId = triggeredFunction.getId();
	}

	public String getTriggeredFunctionId() {
		return this.triggeredFunctionId;
	}

	public Boolean expectsFile() {
		return this.containsFile;
	}

}
