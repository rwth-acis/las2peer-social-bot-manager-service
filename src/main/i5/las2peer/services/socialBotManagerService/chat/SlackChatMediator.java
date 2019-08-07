package i5.las2peer.services.socialBotManagerService.chat;

import java.io.IOException;
import java.util.ArrayList;

import javax.websocket.DeploymentException;

import net.minidev.json.JSONObject;

import com.github.seratch.jslack.*;
import com.github.seratch.jslack.api.rtm.*;
import com.github.seratch.jslack.api.rtm.message.Message;

public class SlackChatMediator extends ChatMediator {
	private RTMClient rtm = null;
	private SlackChatMessageCollector messageCollector = new SlackChatMessageCollector();
	
	private String botUser;

	public SlackChatMediator(String authToken) throws IOException, DeploymentException {
		super(authToken);
		rtm = new Slack().rtm(authToken);

		rtm.addMessageHandler(messageCollector);
		rtm.connect();
		this.botUser = rtm.getConnectedBotUser().toString();
	}
	
	public void sendMessageToChannel(String channel, String text, long id) {	
		rtm.sendMessage(Message
				        .builder()
				        .id(id)
				        .channel(channel)
				        .text(text)
				        .build()
				        .toJSONString());
	}
	
	// static for calling from `SlackChatMessageCollector`
	public static ChatMessage parseSlackMessage(JSONObject o) throws InvalidChatMessageException {
		String channel = o.getAsString("channel");
		String user = o.getAsString("user");
		String text = o.getAsString("text");
		
		if (channel == null || user == null || text == null) {
			throw new InvalidChatMessageException();
		}
		
		return new ChatMessage(channel, user, text);
	}
	
	public ChatMessage parseMessage(JSONObject o) throws InvalidChatMessageException {
		return parseSlackMessage(o);
	}
	
	public ArrayList<ChatMessage> getMessages() {
		return this.messageCollector.getMessages();
	}
	
	public String getUser() {
		return this.botUser.toString();
	}
}
