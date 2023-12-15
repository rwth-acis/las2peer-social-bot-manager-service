package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class IncomingMessage {
	String intentKeyword;
	String intentLabel;
	String followupMessageType;
	String entityKeyword;
	String NluID;
	boolean containsFile;
	String triggeredFunctionId;
	HashMap<IncomingMessage, String> triggerEntity;
	String fileURL;
	String errorMessage;
	String type;
	boolean openAIEnhance;
	boolean freezeMessageSend;
	boolean isRateable;
	boolean asynchron;

	/**
	 * Conversation Id for the message
	 * A conversation is a sequence of messages between the bot and the user. A
	 * conversation ends if there are no followup messages
	 */
	UUID conversationId;

	ArrayList<String> responses;

	/*
	 * List of followup messages. Followup messages are messages that are connected
	 * to
	 * the current message via a leadsTo relation.
	 * The key is the intent keyword that triggers the
	 * followup message.
	 */
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


	public IncomingMessage(String intent, String NluID, Boolean containsFile, ArrayList<String> responses,
			String fileURL, String errorMessage, String type, String intentLabel, String followupType, Boolean isRateable, Boolean asynchron) {
		if (intent != "") {
			this.intentKeyword = replaceUmlaute(intent);
		} else
			intentKeyword = "";
		this.followupMessages = new HashMap<String, IncomingMessage>();
		this.responses = responses;
		this.containsFile = containsFile;
		if (intentKeyword.equals("0") && containsFile) {
			intentKeyword = "anyFile";
		}
		if (NluID == "") {
			this.NluID = "";
		} else
			this.NluID = NluID;

		this.fileURL = fileURL;
		this.errorMessage = errorMessage;
		this.triggerEntity = new HashMap<IncomingMessage, String>();
		this.type = type;
		this.followupMessageType = followupType;
		this.intentLabel = intentLabel;
		if(isRateable!=null){
			this.isRateable = isRateable;
		}else{
			this.isRateable = false;
		}
		if(asynchron!=null) {
			this.asynchron = asynchron;
		} else {
			this.asynchron = false;
		}
	}

	public UUID getConversationId() {
		return conversationId;
	}

	public void setConversationId(UUID conversationId) {
		this.conversationId = conversationId;
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

	/**
	 * Followup messages are messages that are connected
	 * to the current message via a leadsTo relation.
	 * 
	 * @return A HashMap of followup messages. The key is the intent keyword that
	 *         triggers the followup message.
	 */
	public HashMap<String, IncomingMessage> getFollowingMessages() {
		return followupMessages;
	}

	/**
	 * Adds a followup message to the list of followup messages. The intentKeyword
	 * 
	 * @param intentKeyword The intent keyword that triggers the followup message
	 * @param msg           The followup message
	 */
	public void addFollowupMessage(String intentKeyword, IncomingMessage msg) {
		String[] intentList = intentKeyword.split(",");
		for (String intent : intentList) {
			if (intent.equals("") && msg.containsFile) {
				this.followupMessages.put(replaceUmlaute(intent).replaceAll("\\s+", "") + "anyFile", msg);
			} else {
				this.followupMessages.put(replaceUmlaute(intent).replaceAll("\\s+", ""), msg);
			}
		}
		// (this.followupMessages.put(replaceUmlaute(intentKeyword), msg);
	}

	public String getResponse(Random random) {
		if (responses.isEmpty()) {
			return null;
		} else {
			return responses.get(random.nextInt(responses.size()));
		}
	}

	public ArrayList<String> getResponseArray() {
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

	public String getFileURL() {
		return fileURL;
	}

	public boolean getOpenAIEnhance() {
		return this.openAIEnhance;
	}

	public void setOpenAIEnhance(boolean flag) {
		this.openAIEnhance = flag;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public String getTriggerEntity(IncomingMessage m){
      return this.triggerEntity.get(m);
  	}

	public void setTriggeredFunctionId(String functionId) {
		this.triggeredFunctionId = functionId;
	}

	public void addTriggerEntity(IncomingMessage m, String triggerEntity) {
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

	public void setFreezeMessageSend(boolean flag) {
		this.freezeMessageSend = flag;
	}

	public boolean isRateable() {
		return isRateable;
	}

	public void setRateable(boolean isRateable) {
		this.isRateable = isRateable;
	}

	public boolean asynchron() {
		return asynchron;
	}

	public void setAsynchron(boolean asynchron) {
		this.asynchron = asynchron;
	}

	public boolean getAsynchron() {
		return asynchron;
	}
}
