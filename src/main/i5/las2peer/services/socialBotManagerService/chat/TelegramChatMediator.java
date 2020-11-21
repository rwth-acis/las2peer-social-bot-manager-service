package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.OptionalLong;

import javax.ws.rs.core.MediaType;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.MessageFile;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import net.minidev.json.JSONObject;

/**
 * The TelegramChatMediator handles the communication with the Telegram
 * Messenger Application. This includes the parsing of incoming event data and
 * the sending of data to telegram channels.
 *
 */
public class TelegramChatMediator extends EventChatMediator {

	/**
	 * URL address of the SBF manager service
	 */
	private final static String url = "https://973cc7330f55.ngrok.io";
	MiniClient client;

	public TelegramChatMediator(String authToken) {
		super(authToken);

		this.client = new MiniClient();
		client.setConnectorEndpoint("https://api.telegram.org/bot" + authToken);

		this.settingWebhook();
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
	public ChatMessage handleEvent(JSONObject event) {
		assert event != null : "jsonobject event parameter is null";

		try {

			JSONObject message = (JSONObject) event.get("message");
			JSONObject chat = (JSONObject) message.get("chat");
			JSONObject from = (JSONObject) message.get("from");
			String channel = chat.getAsString("id");
			String user = from.getAsString("first_name");
			String text = message.getAsString("text");
			String timestamp = message.getAsString("date");

			if (channel == null || user == null || text == null || timestamp == null)
				throw new InvalidChatMessageException("missing message fields");

			ChatMessage chatMessage = new ChatMessage(channel, user, text, timestamp);

			// check command
			if (text.startsWith("/")) {
				String command = text.substring(1).split(" ")[0];
				System.out.println(command);
				chatMessage.setCommand(command);
			}

			return chatMessage;

		} catch (InvalidChatMessageException e) {
			e.printStackTrace();
			return null;
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

		ClientResponse result = client.sendRequest("GET", "setWebhook?url=" + url + path + super.authToken,
				MediaType.TEXT_PLAIN);
		System.out.println(result.getResponse());
	}

	/**
	 * Sends a plain text message to telegram messenger channel
	 */
	@Override
	public void sendMessageToChannel(String channel, String text, OptionalLong id) {

		System.out.println("send message to telegram channel " + channel + ", size: " + text.length());
		assert channel != null;
		assert text != null;
		
		TelegramBot bot = new TelegramBot(authToken);
		SendMessage request = new SendMessage(channel, text);
		request.parseMode(ParseMode.Markdown);
		request.replyMarkup(new ReplyKeyboardRemove());

		BaseResponse res = bot.execute(request);
		if (res.isOk())
			return;

		System.out.println("Response[ errorCode: " + res.errorCode() + ", description: " + res.description());

		request = new SendMessage(channel, text);
		res = bot.execute(request);
		System.out.println(res);

	}

	@Override
	public void sendMessageToChannel(ResponseMessage response) {

		assert response != null : "response parameter is null";
		assert response.getChannel() != null : "response has no channel";

		String channel = response.getChannel();
		
		if (response.getFile() != null) {
			sendFileToChannel(channel, response);
			return;
		}


		if (response.getButtons() == null || response.getButtons().isEmpty()) {
			sendMessageToChannel(channel, response.getMessage());
			return;
		}

		String text = response.getMessage();
		System.out.println("send telegram message: " + text);

		String button = "";
		if (response.hasButtons()) {
			button = button.concat("&reply_markup={\"keyboard\":[[");
			for (String val : response.getButtons()) {
				button = button.concat("\"" + val + "\",");
			}
			button = button.substring(0, button.length() - 1);
			button = button.concat("]],\"one_time_keyboard\":true}");
		}

		try {
			text = URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String markdown = "&parse_mode=Markdown";
		String chat = "&chat_id=" + channel;

		ClientResponse result = client.sendRequest("POST", "sendmessage?text=" + text + chat + markdown + button,
				MediaType.TEXT_PLAIN);

		System.out.println(result.getResponse());

	}

	/**
	 * Sends a file to an telegram channel
	 */
	public void sendFileToChannel(String channel, ResponseMessage response) {

		assert response != null : "resposne is null";
		assert response.getFile() != null : "response has no file";

		MessageFile file = response.getFile();
		String data = file.getDataString();
		String name = file.getName();
		String caption = "";

		byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
		TelegramBot bot = new TelegramBot(authToken);
		SendDocument request = new SendDocument(458385566, bytes);

		if (caption != null)
			request.caption(caption);
		if (name != null)
			request.fileName(name);

		BaseResponse res = bot.execute(request);
		System.out.println(res.description());
	}

}
