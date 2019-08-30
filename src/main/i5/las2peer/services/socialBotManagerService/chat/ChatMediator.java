package i5.las2peer.services.socialBotManagerService.chat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

import net.minidev.json.JSONObject;

public abstract class ChatMediator {
	protected String authToken;

	public ChatMediator(String authToken) {
		this.authToken = authToken;
	}

	public abstract void sendMessageToChannel(String channel, String text, OptionalLong id);

	public void sendMessageToChannel(String channel, String text) {
		sendMessageToChannel(channel, text, OptionalLong.empty());
	}

	public abstract ChatMessage parseMessage(JSONObject o) throws InvalidChatMessageException;

	public abstract ArrayList<ChatMessage> getMessages();

	protected String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (first)
				first = false;
			else
				result.append("&");
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return result.toString();
	}
}
