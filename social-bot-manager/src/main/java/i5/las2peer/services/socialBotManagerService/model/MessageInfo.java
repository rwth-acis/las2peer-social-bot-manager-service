package i5.las2peer.services.socialBotManagerService.model;

import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;

public class MessageInfo {
	ChatMessage message;
	Intent intent;
	String triggeredFunctionId;
	String botName;
	String serviceAlias;
	boolean contextWithService;
	Collection<Entity> recognizedEntities;

	public MessageInfo(ChatMessage message, Intent intent, String triggeredFunctionId, String botName,
			String serviceAlias, boolean contextWithService, Collection<Entity> recognizedEntities) {
		this.message = message;
		this.intent = intent;
		this.triggeredFunctionId = triggeredFunctionId;
		this.botName = botName;
		this.serviceAlias = serviceAlias;
		this.contextWithService = contextWithService;
		this.recognizedEntities = recognizedEntities;
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

	public void setRecognizedEntities(Collection<Entity> entities) {
		this.recognizedEntities = entities;
	}

	public Collection<Entity> getRecognizedEntities() {
		return this.recognizedEntities;
	}
}
