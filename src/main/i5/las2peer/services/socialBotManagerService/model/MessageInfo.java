package i5.las2peer.services.socialBotManagerService.model;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class MessageInfo {
	private ChatMessage message;
	private Intent intent;
	String triggeredFunctionId;
	String botName;
	String serviceAlias;
	boolean contextWithService;

	public MessageInfo(ChatMessage message, Intent intent, String triggeredFunctionId, String botName,
			String serviceAlias, boolean contextWithService) {
		this.setMessage(message);
		this.setIntent(intent);
		this.triggeredFunctionId = triggeredFunctionId;
		this.botName = botName;
		this.serviceAlias = serviceAlias;
		this.contextWithService = contextWithService;
	}

	public MessageInfo() {

	}

	public ChatMessage getMessage() {
		return this.message;
	}

	public Intent getIntent() {
		return this.intent;
	}

	public String getTriggeredFunctionId() {
		return this.triggeredFunctionId;
	}

	public String getBotName() {
		return this.botName;
	}

	public String getServiceAlias() {
		return this.serviceAlias;
	}

	public boolean contextActive() {
		return this.contextWithService;
	}

	public void setIntent(Intent intent) {
	    this.intent = intent;
	}

	public void setMessage(ChatMessage message) {
	    this.message = message;
	}

    @Override
    public String toString() {
	return "MessageInfo: [" + this.message.toString() + ", " + this.intent.toString() + "]";
    }
}
