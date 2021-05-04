package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import javax.websocket.DeploymentException;

import com.github.seratch.jslack.api.model.*;
import com.github.seratch.jslack.api.model.block.ActionsBlock;
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

public class SlackChatMediator extends ChatMediator {
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
	public void sendMessageToChannel(String channel, String text, OptionalLong id) {
		MessageBuilder msg = Message.builder().id(System.currentTimeMillis()).channel(channel).text(text);
		if (id.isPresent()) {
			msg.id(id.getAsLong());
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

	private List<LayoutBlock> parseBlocks(String blocks) {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONArray block = (JSONArray) parser.parse(blocks);
			ArrayList<BlockElement> bList = new ArrayList<>();
			ArrayList<OptionObject> oList = new ArrayList<>();
			JSONObject elementJson = new JSONObject();
			JSONObject optionJson = new JSONObject();
			JSONObject eTextJson = new JSONObject();
			List<LayoutBlock> lb = new ArrayList<>();
			String elements = "";
			String options = "";
			String textString = "";
			PlainTextObject tempPlainText = new PlainTextObject();
			String eTextString = "";
			for (int i = 0; i < block.size(); i++) {
				JSONObject o = (JSONObject) block.get(i);
				//System.out.println(o.toString());
				if (o.containsKey("text")) {
					textString = o.getAsString("text");
					JSONObject textJSON = (JSONObject) parser.parse(textString);

					tempPlainText = PlainTextObject.builder()
							.text(textJSON.getAsString("text"))
							.build();

					//System.out.println(tempPlainText);

					SectionBlock sectionBlock = SectionBlock.builder()
							.text(tempPlainText)
							.build();

					//System.out.println(sectionBlock);

					lb.add(sectionBlock);
				}
				if (o.containsKey("elements")) {
					elements = o.getAsString("elements");
					JSONArray elementsJson = (JSONArray) parser.parse(elements);

					for (int x = 0; x < elementsJson.size(); x++) {
						//System.out.println("element curr: " + elementJson);
						elementJson = (JSONObject) elementsJson.get(x);
						if (elementJson.containsKey("text")) {
							//System.out.println("element " + x + " text element" + elementJson.toString());
							eTextString = elementJson.getAsString("text");
							eTextJson = (JSONObject) parser.parse(eTextString);

							PlainTextObject eTempPlainText = PlainTextObject.builder()
									.text(eTextJson.getAsString("text"))
									.build();

							if (elementJson.getAsString("type").equals("button")) {
								ButtonElement buttonElement = ButtonElement.builder()
										.text(eTempPlainText)
										.build();

								bList.add(buttonElement);
							}
						} else if (elementJson.getAsString("type").equals("checkboxes")) {
							//System.out.println("element " + x + " checkbox element" + elementJson.toString());
							options = elementJson.getAsString("options");
							JSONArray optionsJson = (JSONArray) parser.parse(options);
							//System.out.println(optionsJson.toString());

							for (int z = 0; z < optionsJson.size(); z++) {
								optionJson = (JSONObject) optionsJson.get(z);
								String value = optionJson.getAsString("value");
								String optionString = optionJson.getAsString("text");
								JSONObject currOptionsJSON = (JSONObject) parser.parse(optionString);

								PlainTextObject oTempPlainText = PlainTextObject.builder()
										.text(currOptionsJSON.getAsString("text"))
										.build();

								//System.out.println("text" + oTempPlainText);

								OptionObject oTempOptionObject = OptionObject.builder()
										.value(currOptionsJSON.getAsString("value"))
										.text(oTempPlainText)
										.value(value)
										.build();

								//System.out.println("options" + oTempOptionObject);
								oList.add(oTempOptionObject);
							}

							CheckboxesElement checkboxesElement = CheckboxesElement.builder()
									.options(oList)
									.build();

							bList.add(checkboxesElement);
						} else if (elementJson.getAsString("type").equals("radio_buttons")) {
							//System.out.println("element " + x + " checkbox element" + elementJson.toString());
							options = elementJson.getAsString("options");
							JSONArray optionsJson = (JSONArray) parser.parse(options);
							//System.out.println(optionsJson.toString());

							for (int z = 0; z < optionsJson.size(); z++) {
								optionJson = (JSONObject) optionsJson.get(z);
								String value = optionJson.getAsString("value");
								String optionString = optionJson.getAsString("text");
								JSONObject currOptionsJSON = (JSONObject) parser.parse(optionString);

								PlainTextObject oTempPlainText = PlainTextObject.builder()
										.text(currOptionsJSON.getAsString("text"))
										.build();

								//System.out.println("text" + oTempPlainText);

								OptionObject oTempOptionObject = OptionObject.builder()
										.value(currOptionsJSON.getAsString("value"))
										.text(oTempPlainText)
										.value(value)
										.build();

								//System.out.println("options" + oTempOptionObject);
								oList.add(oTempOptionObject);
							}

							RadioButtonsElement radioButtonsElement = RadioButtonsElement.builder()
									.options(oList)
									.build();

							bList.add(radioButtonsElement);
						}

					}


				}

			}

			//System.out.println("blist: " + bList);
			if(!bList.isEmpty()){
				ActionsBlock tempActionsBlockElement = ActionsBlock.builder()
						.elements(bList)
						.build();

				//System.out.println("tempactions: " + tempActionsBlockElement);
				lb.add(tempActionsBlockElement);

			}

			return lb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public void sendBlocksMessageToChannel(String channel, String blocks, OptionalLong id) {
		//System.out.println("sending blocks now...");

		List<LayoutBlock> lb = parseBlocks(blocks);
		MessageBuilder msg = Message.builder()
				.id(System.currentTimeMillis())
				.channel(channel)
				.blocks(lb);

		if (id.isPresent()) {
			msg.id(id.getAsLong());
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
					.blocks(lb).iconUrl(url).username(name));
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


	@Override
	public void sendAttachmentMessageToChannel(String channel, String attachments, OptionalLong id) {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONArray attachment = (JSONArray) parser.parse(attachments);
			ArrayList<Attachment> attachmentList = new ArrayList<>();
			List<LayoutBlock> lb = new ArrayList<>();

			System.out.println("attachments: " + attachments);
			for(Object arrayObject : attachment) {
				String blocks = ((JSONObject) arrayObject).getAsString("blocks");
				System.out.println("blocks: " + blocks);
				lb = parseBlocks(blocks);
			}

			Attachment a = Attachment.builder().blocks(lb).build();
			System.out.println("attachment a: " + a);
			attachmentList.add(a);
			System.out.println("attachmentsList: " + attachmentList.toString());
			MessageBuilder msg = Message.builder()
					.id(System.currentTimeMillis())
					.channel(channel)
					.attachments(attachmentList);

			if (id.isPresent()) {
				msg.id(id.getAsLong());
			}
			String message = msg.build().toJSONString();
			System.out.println("message after adding attachments: " + message);

			try {
				// make sure that the bot's name and profile pic is used
				String userId = (slack.methods().authTest(req -> req.token(authToken))).getUserId();
				String url = slack.methods().usersInfo(req -> req.token(authToken).user(userId)).getUser().getProfile()
						.getImageOriginal();
				String name = slack.methods().usersInfo(req -> req.token(authToken).user(userId)).getUser().getName();

				ChatPostMessageResponse response = slack.methods(authToken).chatPostMessage(req -> req.channel(channel) // Channel
						// ID
						.attachments(attachmentList).iconUrl(url).username(name));
				System.out.println("Attachment sent: " + response.isOk());
			} catch (Exception e) {
				this.messageCollector.setConnected(false);
				this.reconnect();
				rtm.sendMessage(message);
				System.out.println("Sent attachment with Exception: " + e.getMessage());
				if (e.getMessage().toLowerCase().equals("timeout")) {
					sendAttachmentMessageToChannel(channel, attachments, id);
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
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	// static for calling from `SlackChatMessageCollector`
	public static ChatMessage parseSlackMessage(JSONObject o) throws InvalidChatMessageException {
		String channel = o.getAsString("channel");
		String user = o.getAsString("user");
		String text = o.getAsString("text");
		// used to identity the message (in case it gets edited)
		String ts = o.getAsString("ts");

		//System.out.println(user);
		//System.out.println(channel);
		//System.out.println(text);

		if(o.containsKey("subtype")){
			if(o.getAsString("subtype").equals("message_changed")){
				System.out.println("message subtype message_changed recognized...");
				JSONParser parser = new JSONParser();
				try{
					String currMessage = o.getAsString("message");
					String prevMessage = o.getAsString("previous_message");

					JSONObject currMessageJson = (JSONObject) parser.parse(currMessage);
					user = currMessageJson.getAsString("user");
					text = currMessageJson.getAsString("text");

					//System.out.println("creating new chat message: c: " + channel + " u: " + user + " t: " + text + " cm: " + currMessage + " pm: " + prevMessage + " ts: " + ts);

					return new ChatMessage(channel, user, text, ts, currMessage, prevMessage, "");
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

		return new ChatMessage(channel, user, text, ts);
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
		ArrayList<String> channels = new ArrayList<String>();
		channels.add(channel);
		FilesUploadResponse response2;
		try {
			response2 = slack.methods(authToken).filesUpload(req -> req.channels(channels).file(f)
					.content("Pretty stuff").filename(f.getName()).title(f.getName()));
			System.out.println("File sent: " + response2.isOk());
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
	public void sendFileMessageToChannel(String channel, String fileBody, String fileName, String fileType,
			OptionalLong id) {
		byte[] decodedBytes = Base64.getDecoder().decode(fileBody);
		File file = new File(fileName);
		try {
			FileUtils.writeByteArrayToFile(file, decodedBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<String> channels = new ArrayList<String>();
		channels.add(channel);

		FilesUploadResponse response2;
		try {
			response2 = slack.methods(authToken).filesUpload(req -> req.channels(channels).file(file)
					.content("Pretty stuff").filename(fileName).title(fileName));
			System.out.println("File sent: " + response2.isOk());
		} catch (IOException | SlackApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Files.deleteIfExists(Paths.get(fileName));
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
}
