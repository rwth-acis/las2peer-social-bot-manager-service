package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.OptionalLong;
import java.util.Vector;

import javax.websocket.DeploymentException;

// TODO: Currently needed because of class with the same name in this package
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.channels.UsersLookupByEmailResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.conversations.ConversationsListResponse;
import com.github.seratch.jslack.api.model.Conversation;
import com.github.seratch.jslack.api.model.ConversationType;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.github.seratch.jslack.api.rtm.message.Message;
import com.github.seratch.jslack.api.rtm.message.Message.MessageBuilder;

import net.minidev.json.JSONObject;

public class SlackChatMediator extends ChatMediator {
	private Slack slack = null;
	private RTMClient rtm = null;
	private SlackChatMessageCollector messageCollector = new SlackChatMessageCollector();

	private String botUser;

	public SlackChatMediator(String authToken) throws IOException, DeploymentException {
		super(authToken);
		this.slack = new Slack();
		this.rtm = this.slack.rtm(authToken);

		this.rtm.addMessageHandler(messageCollector);
		this.rtm.connect();
		this.botUser = rtm.getConnectedBotUser().toString();
		System.out.println(this.botUser + " connected.");
	}

	@Override
	public void sendMessageToChannel(String channel, String text, OptionalLong id) {
		MessageBuilder msg = Message.builder().id(System.currentTimeMillis()).channel(channel).text(text);
		if (id.isPresent()) {
			msg.id(id.getAsLong());
		}
		String message = msg.build().toJSONString();
		try {
			ChatPostMessageResponse response = slack.methods(authToken).chatPostMessage(req -> req.channel(channel) // Channel
																													// ID
					.text(text));
			System.out.println("Message sent: " + response.isOk());
		} catch (Exception e) {
			this.messageCollector.setConnected(false);
			this.reconnect();
			rtm.sendMessage(message);
			System.out.println("Sent message with Exception: " + e.getMessage());
		}
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

	@Override
	public Vector<ChatMessage> getMessages() {
		Vector<ChatMessage> messages = this.messageCollector.getMessages();
		this.reconnect();
		return messages;
	}

	public String getBotUser() {
		return this.botUser.toString();
	}

	@Override
	public String getChannelByEmail(String email) {
		Slack slack = Slack.getInstance();
		try {
			UsersLookupByEmailResponse lookupByEmailResponse = slack.methods(this.authToken)
					.usersLookupByEmail(req -> req.email(email));
			String userId = lookupByEmailResponse.getUser().getId();
			ConversationsListResponse listResponse = slack.methods(this.authToken)
					.conversationsList(req -> req.excludeArchived(true).types(Arrays.asList(ConversationType.IM)));
			Conversation im = listResponse.getChannels().stream().filter(c -> c.getUser().equals(userId)).findFirst()
					.get();
			return im.getId();
		} catch (IOException | SlackApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void reconnect() {
		if (!this.messageCollector.isConnected()) {
			try {
				this.rtm.close();
				this.slack = new Slack();
				this.rtm = this.slack.rtm(authToken);

				this.rtm.addMessageHandler(messageCollector);
				this.rtm.connect();
				this.botUser = rtm.getConnectedBotUser().toString();
				this.messageCollector.setConnected(true);
				System.out.println(this.botUser + " reconnected.");
			} catch (IOException | DeploymentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void sendFileMessageToChannel(String channel, File f, String text, OptionalLong id) {
		// TODO Auto-generated method stub

	}
	
}
