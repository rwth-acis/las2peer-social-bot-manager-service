package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import javax.websocket.DeploymentException;

import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatUpdateResponse;
import com.github.seratch.jslack.api.model.*;
import com.github.seratch.jslack.api.model.block.ActionsBlock;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.OptionObject;
import com.github.seratch.jslack.api.model.block.composition.PlainTextObject;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.element.*;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.io.FileUtils;

// TODO: Currently needed because of class with the same name in this package
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.channels.UsersLookupByEmailResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.conversations.ConversationsListResponse;
import com.github.seratch.jslack.api.methods.response.files.FilesUploadResponse;
import com.github.seratch.jslack.api.methods.response.users.UsersConversationsResponse;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.github.seratch.jslack.api.rtm.message.Message;
import com.github.seratch.jslack.api.rtm.message.Message.MessageBuilder;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class SlackChatMediator extends EventChatMediator {
	private Slack slack = null;
	private RTMClient rtm = null;
	private SlackChatMessageCollector messageCollector = new SlackChatMessageCollector();
	private String botUser;
	// this variable is only good when using a bot in a private conversation
	public static HashMap<String, String> usersByChannel;
	// Is needed to use the token when downloading user files
	public static HashMap<String, String> botTokens = new HashMap<String, String>();
	// When files are sent, it is not clear whether a bot or user sent them, differentiate using the id of the bot!
	public static ArrayList<String> botIDs = new ArrayList<String>();

	public SlackChatMediator(String authToken) throws IOException, DeploymentException {
		super(authToken);
		this.slack = new Slack();
		this.rtm = this.slack.rtm(authToken);
		usersByChannel = new HashMap<String, String>();
		this.rtm.addMessageHandler(messageCollector);
		this.rtm.connect();
		ArrayList<ConversationType> types = new ArrayList<ConversationType>();
		types.add(ConversationType.IM);
		types.add(ConversationType.MPIM);
		// Search for every channelId a bot has access to and keep the token
		try {
			UsersConversationsResponse test = (slack.methods()
					.usersConversations(req -> req.token(authToken).types(types)));
			for (Conversation c : test.getChannels()) {
				botTokens.put(c.getId(), authToken);
				// Save users' email address
				usersByChannel.put(c.getId(), slack.methods().usersInfo(req -> req.token(authToken).user(c.getUser()))
						.getUser().getProfile().getEmail());
			}
		} catch (IOException | SlackApiException e) {
			System.out.println("Could not retrieve bot channels because of " + e
					+ ". The bot will not be able to download sent files...");
		}
		this.botUser = rtm.getConnectedBotUser().toString();
		botIDs.add(rtm.getConnectedBotUser().getId());
		messageCollector.setDomain("https://slack.com/");
		System.out.println(this.botUser + " connected.");
	}

	@Override
	public void sendMessageToChannel(String channel, String text, Optional<String> id) {
		MessageBuilder msg = Message.builder().id(System.currentTimeMillis()).channel(channel).text(text);
		if (id.isPresent()) {
			msg.id(Long.parseLong(id.get()));
		}
		String message = msg.build().toJSONString();
		try {
			// make sure that the bot's name and profile pic is used
			String userId = (slack.methods().authTest(req -> req.token(authToken))).getUserId();
			String url = slack.methods().usersInfo(req -> req.token(authToken).user(userId)).getUser().getProfile()
					.getImageOriginal();
			String name = slack.methods().usersInfo(req -> req.token(authToken).user(userId)).getUser().getName();

			ChatPostMessageResponse response = slack.methods(authToken).chatPostMessage(req -> req.channel(channel) // Channel
																													// ID
					.text(text).iconUrl(url).username(name));
			System.out.println("Message sent: " + response.isOk());
		} catch (Exception e) {
			this.messageCollector.setConnected(false);
			this.reconnect();
			rtm.sendMessage(message);
			System.out.println("Sent message with Exception: " + e.getMessage());
			if (e.getMessage().toLowerCase().equals("timeout")) {
				sendMessageToChannel(channel, text, id);
			}
		}
		try {
			// get the users email address if not done at the beginning (should only happen if a new user joined the
			// space)
			if (usersByChannel.get(channel) == null) {
				String user = slack.methods().conversationsInfo(req -> req.token(authToken).channel(channel))
						.getChannel().getUser();
				usersByChannel.put(channel, slack.methods().usersInfo(req -> req.token(authToken).user(user)).getUser()
						.getProfile().getEmail());
			}
		} catch (Exception e) {
			System.out.println("Could not extract Email for reason + " + e);
		}

	}


//	@Override
	public void sendBlocksMessageToChannel(String channel, String blocks, Optional<String> id) {
		//System.out.println("sending blocks now...");

		MessageBuilder msg = Message.builder()
				.id(System.currentTimeMillis())
				.channel(channel);

		if (id.isPresent()) {
			msg.id(Long.parseLong(id.get()));
		}
		String message = msg.build().toJSONString();
		//System.out.println("message after adding blocks: " + message);

		try {
			// make sure that the bot's name and profile pic is used
			String userId = (slack.methods().authTest(req -> req.token(authToken))).getUserId();
			String url = slack.methods().usersInfo(req -> req.token(authToken).user(userId)).getUser().getProfile()
					.getImageOriginal();
			String name = slack.methods().usersInfo(req -> req.token(authToken).user(userId)).getUser().getName();

			ChatPostMessageResponse response = slack.methods(authToken).chatPostMessage(req -> req.channel(channel) // Channel
					// ID
					.blocksAsString(blocks).iconUrl(url).username(name));
			System.out.println("Block sent: " + response.isOk());
		} catch (Exception e) {
			this.messageCollector.setConnected(false);
			this.reconnect();
			rtm.sendMessage(message);
			System.out.println("Sent message with Exception: " + e.getMessage());
			if (e.getMessage().toLowerCase().equals("timeout")) {
				// TODO recursive call not the best idea
				sendBlocksMessageToChannel(channel, blocks, id);
			}
		}
		try {
			// get the users email address if not done at the beginning (should only happen if a new user joined the
			// space)
			if (usersByChannel.get(channel) == null) {
				String user = slack.methods().conversationsInfo(req -> req.token(authToken).channel(channel))
						.getChannel().getUser();
				usersByChannel.put(channel, slack.methods().usersInfo(req -> req.token(authToken).user(user)).getUser()
						.getProfile().getEmail());
			}
		} catch (Exception e) {
			System.out.println("Could not extract Email for reason + " + e);
		}

	}

	public void editMessage(String channel, String messageId, String message, Optional<String> id) {
		System.out.println("now trying editing...");
		MessageBuilder msg = Message.builder()
				.id(System.currentTimeMillis())
				.channel(channel);

		if (id.isPresent()) {
			msg.id(Long.parseLong(id.get()));
		}
		String msgString = msg.build().toJSONString();

		try {
			ChatUpdateResponse response = slack.methods(authToken).chatUpdate(req -> req.channel(channel)
					.blocksAsString(message).ts(messageId));
			System.out.println("Chat updated: " + response.isOk());
		} catch (Exception e) {
			this.messageCollector.setConnected(false);
			this.reconnect();
			rtm.sendMessage(msgString);
			System.out.println("Sent message with Exception: " + e.getMessage());
			if (e.getMessage().toLowerCase().equals("timeout")) {
				// TODO recursive call not the best idea
				editMessage(channel, messageId, message, id);
			}
			try {
				// get the users email address if not done at the beginning (should only happen if a new user joined the
				// space)
				if (usersByChannel.get(channel) == null) {
					String user = slack.methods().conversationsInfo(req -> req.token(authToken).channel(channel))
							.getChannel().getUser();
					usersByChannel.put(channel, slack.methods().usersInfo(req -> req.token(authToken).user(user)).getUser()
							.getProfile().getEmail());
				}
			} catch (Exception exception) {
				System.out.println("Could not extract Email for reason + " + exception);
			}
		}
	}

	// static for calling from `SlackChatMessageCollector`
	public static ChatMessage parseSlackMessage(JSONObject o) throws InvalidChatMessageException {
		String channel = o.getAsString("channel");
		String user = o.getAsString("user");
		String text = o.getAsString("text");
		// used to identity the message (in case it gets edited)
		String time = o.getAsString("ts");

		//System.out.println(user);
		//System.out.println(channel);
		//System.out.println(text);

		if(o.containsKey("subtype")){
			if(o.getAsString("subtype").equals("message_changed")){
				JSONParser parser = new JSONParser();
				System.out.println("message subtype message_changed recognized...");

				try{
					String currMessage = o.getAsString("message");
					String prevMessage = o.getAsString("previous_message");

					JSONObject currMessageJson = (JSONObject) parser.parse(currMessage);

					System.out.println("now checking who edited answer...");
					if(currMessageJson.containsKey("subtype")){
						// check if the bot edited an answer
						System.out.println("subtype: " + currMessageJson.getAsString("subtype"));
						if(currMessageJson.getAsString("subtype").equals("bot_message")){
							System.out.println("bot changed answer, ignore");
							throw new InvalidChatMessageException();
						}
					}
					System.out.println("user edited answer");
					// user edited an answer
					user = currMessageJson.getAsString("user");
					text = currMessageJson.getAsString("text");

					//System.out.println("creating new chat message: c: " + channel + " u: " + user + " t: " + text + " cm: " + currMessage + " pm: " + prevMessage + " ts: " + ts);
					ChatMessage msg = new ChatMessage(channel, user, text, time);
					msg.setCurrMessage(currMessage);
					msg.setPreviousMessage(prevMessage);
					return msg;
				} catch(Exception e){
					e.printStackTrace();
				}

			}
		}

		// Second part of the if clause if bcs the bot would for some reason react to its own message
		if (o.get("files") != null && !botIDs.contains(o.get("user"))) {
			for (int i = 0; i < ((JSONArray) o.get("files")).size(); i++) {
				// left it as for(...), but only sending 1 file at a time will be accepted currently
				try {
					URL url = new URL(((JSONObject) ((JSONArray) o.get("files")).get(i)).getAsString("url_private"));
					HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
					httpConn.addRequestProperty("Authorization", "Bearer " + botTokens.get(channel));
					InputStream in = httpConn.getInputStream();
					FileOutputStream fileOutputStream = new FileOutputStream(
							((JSONObject) ((JSONArray) o.get("files")).get(i)).getAsString("title"));
					byte dataBuffer[] = new byte[Integer
							.valueOf(((JSONObject) ((JSONArray) o.get("files")).get(i)).getAsString("size"))];
					int bytesRead;
					while ((bytesRead = in.read(dataBuffer, 0, Integer
							.valueOf(((JSONObject) ((JSONArray) o.get("files")).get(i)).getAsString("size")))) != -1) {
						fileOutputStream.write(dataBuffer, 0, bytesRead);
					}
					String body = Base64.getEncoder()
							.encodeToString(Files.readAllBytes(
									new File(((JSONObject) ((JSONArray) o.get("files")).get(i)).getAsString("title"))
											.toPath()));
					fileOutputStream.close();
					Files.deleteIfExists(
							Paths.get(((JSONObject) ((JSONArray) o.get("files")).get(i)).getAsString("title")));
					return new ChatMessage(channel, user, text,
							((JSONObject) ((JSONArray) o.get("files")).get(i)).getAsString("title"),
							((JSONObject) ((JSONArray) o.get("files")).get(i)).getAsString("filetype"), body);
				} catch (Exception e) {
					System.out.println("Could not extract File for reason " + e);
				}

			}

		}
		if (channel == null || user == null || text == null) {
			throw new InvalidChatMessageException();
		}

		return new ChatMessage(channel, user, text, time);
	}

	@Override
	public Vector<ChatMessage> getMessages() {
		Vector<ChatMessage> messages = this.messageCollector.getMessages();
		this.reconnect();
		return messages;
	}

	/*public String getEmails(String channel) {
		
		if(usersByChannel.get(channel) == null)
		{
			return "No Email available at the moment";
		}
		System.out.println("Email is " + usersByChannel.get(channel));
		return usersByChannel.get(channel); // slack.methods().usersInfo(req -> req.token(authToken).user(user)).getUser().getProfile().getEmail();
	}
	*/
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

	public SlackChatMessageCollector getMessageCollector() {
		return messageCollector;
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
	public void sendFileMessageToChannel(String channel, File f, String text, Optional<String> id) {
		ArrayList<String> channels = new ArrayList<String>();
		channels.add(channel);
		FilesUploadResponse response2;
		try {
			response2 = slack.methods(authToken).filesUpload(req -> req.channels(channels).file(f)
					.content("Pretty stuff").filename(f.getName()).title(f.getName()).initialComment(text));
			System.out.println("File sendt: " + response2.isOk()  + text);
		} catch (IOException | SlackApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Files.deleteIfExists(Paths.get(f.getName()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			this.rtm.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.slack = null;
	}

	@Override
	public void handleEvent(JSONObject action) {
		System.out.println("action: " + action);
		JSONParser p = new JSONParser();

		try{
			System.out.println("now trying to handle message...");
			String ts = ((JSONObject) p.parse(action.getAsString("container"))).getAsString("message_ts");
			String channel = ((JSONObject) p.parse(action.getAsString("channel"))).getAsString("id");
			String user = ((JSONObject) p.parse(action.getAsString("user"))).getAsString("id");
			String text = "";

			JSONArray actions = (JSONArray) p.parse(action.getAsString("actions"));
			for (Object actionsObject : actions) {
				String selectedOptionsString = ((JSONObject) actionsObject).getAsString("selected_options");
				String selectedOptionString = ((JSONObject) actionsObject).getAsString("selected_option");
				if (selectedOptionsString != null) {
					// multiple choice with one or more than one selected option
					// System.out.println("selected options string: " + selectedOptionsString);
					JSONArray selectedOptionsJson = (JSONArray) p.parse(selectedOptionsString);
					text = selectedOptionsJson.toString();

				} else if (selectedOptionString != null) {
					// single choice with one selected option (possible)
					// System.out.println("selected option: " + selectedOptionString);
					JSONObject selectedOptionJson = (JSONObject) p.parse(selectedOptionString);

					String textString = selectedOptionJson.getAsString("text");
					JSONObject textJson = (JSONObject) p.parse(textString);
					text += textJson.getAsString("text");

				} else {
					// System.out.println("No selectedOption and no selectedOptions.");
					System.out.println("No selectedOption and no selectedOptions. Just a normal button press.");

					String textString = ((JSONObject) actionsObject).getAsString("text");
					JSONObject textJson = (JSONObject) p.parse(textString);
					text += textJson.getAsString("text");
				}
			}

			System.out.println("Assembled text from triggerButton is: " + text);
			// remove the last ","
			if ((String.valueOf(text.charAt(text.length() - 1)).equals(","))) {
				System.out.println("inside removing last comma");
				text = text.substring(0, text.length() - 1);
			}

			ChatMessage chatMessage = new ChatMessage(channel, user, text, ts);

			// set email, since it is not passed on in body
			chatMessage.setEmail(user);

			messageCollector.addMessage(chatMessage);
		} catch(Exception e){
			e.printStackTrace();
		}

	}

}
