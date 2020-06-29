package i5.las2peer.services.socialBotManagerService.model;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class MessageInfo {
	ChatMessage message;
	Intent intent;
	String triggeredFunctionId;
	String botName;
	String serviceAlias;

	public MessageInfo(ChatMessage message,	Intent intent,
			String triggeredFunctionId, String botName,
			String serviceAlias)
	{
		this.message = message;
		this.intent = intent;
		this.triggeredFunctionId = triggeredFunctionId;
		this.botName = botName;
		this.serviceAlias = serviceAlias;
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
}
