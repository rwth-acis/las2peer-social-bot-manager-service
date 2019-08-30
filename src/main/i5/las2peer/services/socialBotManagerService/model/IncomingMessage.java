package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashMap;

public class IncomingMessage {
	String intentKeyword;
	String entityKeyword;

	// TODO: Action to perform

	// Intent keywords used as keys
	HashMap<String, IncomingMessage> followupMessages;

	public IncomingMessage(String intent) {
		this.intentKeyword = intent;
		this.followupMessages = new HashMap<String, IncomingMessage>();
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

	public HashMap<String, IncomingMessage> getFollowingMessages() {
		return this.followupMessages;
	}

	public void addFollowupMessage(String intentKeyword, IncomingMessage msg) {
		this.followupMessages.put(intentKeyword, msg);
	}
}
