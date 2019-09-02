package i5.las2peer.services.socialBotManagerService.model;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class MessageInfo {
	ChatMessage message;
	Intent intent;
	String triggeredFunctionId;
	String botAgent;

	public MessageInfo(ChatMessage message,	Intent intent,
			String triggeredFunctionId, String botAgent)
	{
		this.message = message;
		this.intent = intent;
		this.triggeredFunctionId = triggeredFunctionId;
		this.botAgent = botAgent;
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

	public String getBotAgent() {
		return this.botAgent;
	}
}
