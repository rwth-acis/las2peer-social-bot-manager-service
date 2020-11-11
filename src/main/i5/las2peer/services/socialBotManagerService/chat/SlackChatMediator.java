package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.io.IOException;
import java.util.OptionalLong;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.auth.AuthTestResponse;
import com.slack.api.methods.response.bots.BotsInfoResponse;
import com.slack.api.methods.response.bots.BotsInfoResponse.Bot;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import net.minidev.json.JSONObject;

public class SlackChatMediator extends EventChatMediator {

	/**
	 * Main object of the Slack API Java library
	 */
	private Slack slack;

	/**
	 * Team ID of Slack workspace (e.g T018LTPPG1J)
	 */
	private String teamID;

	/**
	 * App ID of Slack application (e.g A018LTPPG1J)
	 */
	private String appID;

	/**
	 * @param authToken used to authenticate the bot when accessing the slack API
	 */
	public SlackChatMediator(String authToken) {
		super(authToken);
		this.slack = Slack.getInstance();
		this.requestAuthTest();
	}

	@Override
	public ChatMessage handleEvent(JSONObject event) {
	    	
	    	ChatMessage message = new ChatMessage();
	    
		String type = (String) event.get("type");
		switch (type) {
		case "message":
			System.out.println("slack event: message");
			if (event.get("bot_id") != null)
				break;
			message = this.addMessage(event);
			break;
		case "app_mention":
			System.out.println("slack event: app mention");
			String channel = (String) event.get("channel");
			String user = (String) event.get("user");
			this.sendMessageToChannel(channel, "hello " + user);
			break;
		case "team_join":
			System.out.println("slack event: team_join");
			this.sendMessageToChannel("C01880R2NPQ", "hello");
			break;
		default:
			System.out.println("unknown slack event received");
		}
		return message;
	}

	/**
	 * Sends an auth.test request to the slack event API. Receives the
	 * team/workspace id of the authenticated bot.
	 */
	private void requestAuthTest() {

		try {
			AuthTestResponse response = slack.methods().authTest(req -> req.token(authToken));
			if (response.isOk()) {
				System.out.println("Bot Authentication: " + response.isOk());
				this.setTeamID(response.getTeamId());
			} else {
				System.out.println(response.getError());
			}
		} catch (SlackApiException requestFailure) {
			System.out.println("Slack API responded with unsuccessful status code");
		} catch (IOException connectivityIssue) {
			System.out.println("Failed to connect to Slack API");
		}
	}

	/**
	 * Sends an bots.info request to the slack event API. Requires the users:read
	 * scope in the slack app. Receives the application id of the authenticated bot.
	 */
	private void requestBotInfo() {

		try {
			BotsInfoResponse response = slack.methods().botsInfo(req -> req.token(authToken));
			if (response.isOk()) {
				System.out.println("Bot Info received: " + response.isOk());
				System.out.println(response.toString());
				Bot botInfo = response.getBot();
				System.out.println(botInfo.toString());
				this.setAppID(botInfo.getAppId());
			} else {
				System.out.println(response.getError());
			}
		} catch (SlackApiException requestFailure) {
			System.out.println("Slack API responded with unsuccessful status code");
		} catch (IOException connectivityIssue) {
			System.out.println("Failed to connect to Slack API");
		}
	}

	/**
     * Adds a message to the message collection
     * 
     * @param parsedMessage The Slack message event in JSON format
     */
	public ChatMessage addMessage(JSONObject parsedMessage) {

		try {
			String type = parsedMessage.getAsString("type");
			if (type == null || !type.equals("message"))
				throw new InvalidChatMessageException("not a message type");

			String channel = parsedMessage.getAsString("channel");
			String user = parsedMessage.getAsString("user");
			String text = parsedMessage.getAsString("text");
			String timestamp = parsedMessage.getAsString("ts");

			if (channel == null || user == null || text == null || timestamp == null) {
				throw new InvalidChatMessageException("missing message fields");
			}

			ChatMessage message = new ChatMessage(channel, user, text, timestamp);
			//this.addMessage(message);
			return message;

		} catch (InvalidChatMessageException e) {
			e.printStackTrace();
			return null;
		}
		
	}

    @Override
    public void sendMessageToChannel(String channel, String text, OptionalLong id) {

		try {
			ChatPostMessageResponse response = slack.methods(authToken)
					.chatPostMessage(req -> req.channel(channel).text(text));
			if (response.isOk()) {
				System.out.println("Message sent: " + response.isOk());
			} else {
				System.out.println(response.getError());
			}
		} catch (SlackApiException requestFailure) {
			System.out.println("Slack API responded with unsuccessful status code");
		} catch (IOException connectivityIssue) {
			System.out.println("Failed to connect to Slack API");
		}

	}

	public void sendBlockToChannel(String channel, String text) {

		try {
			ChatPostMessageResponse response = slack.methods(authToken)
					.chatPostMessage(req -> req.channel(channel).blocksAsString(text));
			if (response.isOk()) {
				System.out.println("Message sent: " + response.isOk());
			} else {
				System.out.println(response.getError()); // e.g., "invalid_auth", "channel_not_found"
			}
		} catch (SlackApiException requestFailure) {
			// Slack API responded with unsuccessful status code (= not 20x)
			System.out.println("Slack API responded with unsuccessful status code");
		} catch (IOException connectivityIssue) {
			System.out.println("Failed to connect to Slack API");
		}

	}

	@Override
	public void sendFileMessageToChannel(String channel, File f, String text, OptionalLong id) {
		// TODO Auto-generated method stub

	}

	public String getTeamID() {
		return this.teamID;
	}

	public void setTeamID(String teamID) {
		this.teamID = teamID;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}


    @Override
    public void sendFileToChannel(String channel, ResponseMessage response) {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean sendMessageToChannel(ResponseMessage response) {
	return false;

    }

}
