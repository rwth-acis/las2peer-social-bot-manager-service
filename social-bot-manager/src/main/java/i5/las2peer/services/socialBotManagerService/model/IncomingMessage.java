package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class IncomingMessage {
	String intentKeyword;
	String intentLabel;
	String followupMessageType;
	String entityKeyword;
    String NluID;
    boolean containsFile;
	String response;
	String triggeredFunctionId;
	HashMap<IncomingMessage,String> triggerEntity;
	String fileURL;
	String errorMessage;
	String type;

	ArrayList<IncomingMessage> responses;

	// Intent keywords used as keys
	HashMap<String, IncomingMessage> followupMessages;


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


	public IncomingMessage(String intent, String NluID, Boolean containsFile,String response, String fileURL, String errorMessage, String type,String intentLabel, String followupType) {
		if(intent != "") {
			this.intentKeyword = replaceUmlaute(intent);
		} else intentKeyword = "";
		this.followupMessages = new HashMap<String, IncomingMessage>();
		this.responses = new ArrayList<IncomingMessage>();
		this.containsFile = containsFile;
		if (intentKeyword.equals("0") && containsFile){
			intentKeyword = "anyFile";
		}
        if(NluID == ""){
            this.NluID = "";
        } else this.NluID = NluID;

		
		this.response = response;
		this.fileURL = fileURL;
		this.errorMessage  = errorMessage;
		this.triggerEntity = new HashMap<IncomingMessage,String>();
		this.type = type;
		this.followupMessageType = followupType;
		this.intentLabel = intentLabel;
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
		String[] intentList = intentKeyword.split(",");
		for (String intent : intentList) {
			if (intent.equals("") && msg.containsFile){
				this.followupMessages.put(replaceUmlaute(intent).replaceAll("\\s+", "") + "anyFile", msg);
			} else {
				this.followupMessages.put(replaceUmlaute(intent).replaceAll("\\s+", ""), msg);
			}
		}
		// (this.followupMessages.put(replaceUmlaute(intentKeyword), msg);
	}

	public void addResponse(IncomingMessage response) {
		this.responses.add(response);
	}

	public IncomingMessage getResponse(Random random) {
		if (responses.isEmpty()) {
			return null;
		} else {
			return responses.get(random.nextInt(responses.size()));
		}
	}
	
	public ArrayList<IncomingMessage> getResponseArray() {
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

	public boolean expectsFile() {
		return this.containsFile;
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

	public String getIntentLabel() {
		return intentLabel;
	}


	public void setIntentLabel(String intentLabel) {
		this.intentLabel = intentLabel;
	}


	public String getFollowupMessageType() {
		return followupMessageType;
	}


	public void setFollowupMessageType(String followupMessageType) {
		this.followupMessageType = followupMessageType;
	}
}
