package i5.las2peer.services.socialBotManagerService.chat;

import net.minidev.json.JSONObject;

public abstract class ChatMediator {
	protected String authToken;

	public ChatMediator(String authToken) {
		this.authToken = authToken;
	}

	public abstract void sendMessageToChannel(String channel, String text, long id);

	public abstract ChatMessage parseMessage(JSONObject o) throws InvalidChatMessageException;
}
