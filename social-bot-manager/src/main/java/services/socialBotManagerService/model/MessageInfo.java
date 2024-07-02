package services.socialBotManagerService.model;

import java.util.Collection;
import java.util.UUID;

import services.socialBotManagerService.chat.ChatMessage;
import services.socialBotManagerService.nlu.Entity;
import services.socialBotManagerService.nlu.Intent;

public class MessageInfo {
	ChatMessage message;
	Intent intent;
	String triggeredFunctionId;
	String botName;
	String serviceAlias;
	boolean contextWithService;
	Collection<Entity> recognizedEntities;
	String conversationId;
	String messengerName = "";



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

	public MessageInfo(ChatMessage message, Intent intent, String triggeredFunctionId, String botName,
			String serviceAlias, boolean contextWithService, Collection<Entity> recognizedEntities, String messengerName) {
		this.message = message;
		this.intent = intent;
		this.triggeredFunctionId = triggeredFunctionId;
		this.botName = botName;
		this.serviceAlias = serviceAlias;
		this.contextWithService = contextWithService;
		this.recognizedEntities = recognizedEntities;
		this.messengerName = messengerName;
	}

	public MessageInfo(ChatMessage message, Intent intent, String triggeredFunctionId, String botName,
			String serviceAlias, boolean contextWithService, Collection<Entity> recognizedEntities,
			String messengerName, UUID conversationId) {
		this.message = message;
		this.intent = intent;
		this.triggeredFunctionId = triggeredFunctionId;
		this.botName = botName;
		this.serviceAlias = serviceAlias;
		this.contextWithService = contextWithService;
		this.recognizedEntities = recognizedEntities;
		this.messengerName = messengerName;
		this.conversationId = conversationId.toString();
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

	public String getMessengerName() {
		return messengerName;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setMessengerName(String messengerName) {
		this.messengerName = messengerName;
	}
}
