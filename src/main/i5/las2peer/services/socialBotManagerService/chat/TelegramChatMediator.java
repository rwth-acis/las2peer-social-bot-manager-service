package i5.las2peer.services.socialBotManagerService.chat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Vector;

import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.GetMeResponse;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import net.minidev.json.JSONObject;

/**
 * The TelegramChatMediator handles the communication with the Telegram
 * Messenger Application. This includes the parsing of incoming event data and
 * the sending of data to telegram channels.
 *
 */
public class TelegramChatMediator extends EventChatMediator {

	TelegramBot bot;
	private TelegramMessageCollector messageCollector;

	/**
	 * URL address of the SBF manager service
	 */
	private final String url;
	MiniClient client;

	public TelegramChatMediator(String authToken, String url) {
		super(authToken);

		this.bot = new TelegramBot(authToken);
		this.client = new MiniClient();
		this.url = url;
		client.setConnectorEndpoint("https://api.telegram.org/bot" + authToken);

		this.settingWebhook();
		messageCollector = new TelegramMessageCollector();
	}

	@Override
	public Vector<ChatMessage> getMessages() {
		return messageCollector.getMessages();
	}

	/**
	 * Handle incoming telegram event message.
	 * 
	 * @param event telegram event as json object
	 * @see <a href="https://core.telegram.org/bots/api#getting-updates">Getting
	 *      Telegram Updates</a>
	 *
	 */
	@Override
	public void handleEvent(JSONObject event) {
		assert event != null : "jsonobject event parameter is null";

		try {
			JSONObject message = (JSONObject) event.get("message");
			JSONObject chat = (JSONObject) message.get("chat");
			JSONObject from = (JSONObject) message.get("from");
			JSONObject document = (JSONObject) message.get("document");
			String channel = chat.getAsString("id");
			String user = from.getAsString("first_name");
			String text = message.getAsString("text");
			String timestamp = message.getAsString("date");

			if (channel == null || user == null || (text == null && document == null) || timestamp == null)
				throw new InvalidChatMessageException("missing message fields");

			this.showAction(channel, ChatAction.typing);

			// message with document
			if (document != null) {
				String fileName = document.getAsString("file_name");
				String mimeType = document.getAsString("mime_type");
				String fileId = document.getAsString("file_id");
				String fileBody = getFile(fileId);
				messageCollector.addMessage(new ChatMessage(channel, user, text, timestamp, fileName, mimeType,
											fileBody));
			} else {
				messageCollector.addMessage(new ChatMessage(channel, user, text, timestamp));
			}
		} catch (InvalidChatMessageException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Registers to receive push notifications from telegram
	 *
	 * @see <a href="https://core.telegram.org/bots/api#setwebhook">Setting Telegram
	 *      Webhook</a>
	 */
	public void settingWebhook() {
		assert url != null : "url not initialized";
		assert !url.contentEquals("") : "empty url";

		String path = "/sbfmanager/bots/events/telegram/";
		if (url.endsWith("/sbfmanager")) {
			path = "/bots/events/telegram/";
		}

		System.out.println("Setting Webhook");
		ClientResponse result = client.sendRequest("GET", "setWebhook?url=" + url + path + super.authToken,
				MediaType.TEXT_PLAIN);
		System.out.println(result.getResponse());
	}

	public String getBotName() {
		GetMe request = new GetMe();
		GetMeResponse response = bot.execute(request);
		if (response.isOk() && response.user() != null) {
			
			String username = response.user().username();
			System.out.println("request botname: " + username);
			return username;
		}
		
		return null;
	}

	/**
	 * Sends a plain text message to telegram messenger channel
	 */
	@Override
	public void sendMessageToChannel(String channel, String text, Optional<String> id) {

		System.out.println("send plain message to telegram channel " + channel + ", size: " + text.length());
		assert channel != null;
		assert text != null;

		SendMessage request = new SendMessage(channel, text);
		BaseResponse res = bot.execute(request);

	}

	@Override
	public void sendFileMessageToChannel(String channel, String fileBody, String fileName, String fileType, String text,
										 Optional<String> id) {
		String caption = "";
		System.out.println("Send File to Telegram channel: " + channel);

		try {

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(fileBody);
			fileBody = gson.toJson(je);
			System.out.println(fileBody.substring(0, 160));

		} catch (Exception e) {
			e.printStackTrace();
		}

		byte[] bytes = fileBody.getBytes(StandardCharsets.UTF_8);
		SendDocument request = new SendDocument(channel, bytes);

		if (!caption.contentEquals(""))
			request.caption(caption);
		if (fileName != null && !fileName.contentEquals(""))
			request.fileName(fileName);

		request.replyMarkup(new ReplyKeyboardRemove());
		BaseResponse res = bot.execute(request);
		System.out.println(String.valueOf(res.isOk()) + " " + res.errorCode() + " " + res.description());
	}

	@Override
	public void sendFileMessageToChannel(String channel, java.io.File f, String text, Optional<String> id) {
		String caption = "";
		System.out.println("Send File to Telegram channel: " + channel);

		SendDocument request = new SendDocument(channel, f);

		if (!caption.contentEquals(""))
			request.caption(caption);

		request.replyMarkup(new ReplyKeyboardRemove());
		BaseResponse res = bot.execute(request);
		System.out.println(String.valueOf(res.isOk()) + " " + res.errorCode() + " " + res.description());
	}


	@Override
	public String getChannelByEmail(String email) {
		return null;
	}

	@Override
	public void close() {

	}

	private String getFile(String fileId) {

		GetFile request = new GetFile(fileId);
		GetFileResponse response = bot.execute(request);
		File file = response.file();
		System.out.println("file received");
		String path = "https://api.telegram.org/file/bot" + authToken + "/" + file.filePath();
		URL url;
		String data = null;
		try {
			url = new URL(path);
			data = IOUtils.toString(url);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	@Override
	public void sendBlocksMessageToChannel(String channel, String blocks, Optional<String> id) {

	}

	@Override
	public void sendBlocksMessageToChannel(String channel, String blocks) {
		super.sendBlocksMessageToChannel(channel, blocks);
	}

	@Override
	public void sendAttachmentMessageToChannel(String channel, String attachments, Optional<String> id) {

	}

	@Override
	public void sendAttachmentMessageToChannel(String channel, String attachments) {
		super.sendAttachmentMessageToChannel(channel, attachments);
	}

	/**
	 * Shows an indication to the user about what the next bots action is
	 * 
	 * @param channel id of channel indication should be shown
	 * @param action type of indication shown
	 * @return request was successful (true) or failed (false)
	 */
	public boolean showAction(String channel, ChatAction action) {

		SendChatAction typingAction = new SendChatAction(channel, action);
		BaseResponse response = this.bot.execute(typingAction);
		return response.isOk();
	}



}
