package i5.las2peer.services.socialBotManagerService.chat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.OptionalLong;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
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

	TelegramBot bot;

	/**
	 * URL address of the SBF manager service
	 */
	private final static String url = "https://477e0f80549b.ngrok.io";
	MiniClient client;

	public TelegramChatMediator(String authToken) {
		super(authToken);

		this.bot = new TelegramBot(authToken);
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
			JSONObject document = (JSONObject) message.get("document");
			String channel = chat.getAsString("id");
			String user = from.getAsString("first_name");
			String text = message.getAsString("text");
			String timestamp = message.getAsString("date");

			if (channel == null || user == null || (text == null && document == null) || timestamp == null)
				throw new InvalidChatMessageException("missing message fields");

			this.showAction(channel, ChatAction.typing);
			ChatMessage chatMessage = new ChatMessage(channel, user, text, timestamp);

			// message with document
			if (document != null) {
				String fileName = document.getAsString("file_name");
				String mimeType = document.getAsString("mime_type");
				String fileId = document.getAsString("file_id");
				String fileUniqueId = document.getAsString("file_unique_id");
				String fileSize = document.getAsString("file_size");
				MessageFile fileMessage = getFile(fileId);
				fileMessage.setName(fileName);
				chatMessage.setFileContent(fileMessage.getDataString());
				chatMessage.setText(fileMessage.getDataString());
			}

			// check command
			else if (text.startsWith("/")) {
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
	public void sendMessageToChannel(String channel, String text, OptionalLong id) {

		System.out.println("send plain message to telegram channel " + channel + ", size: " + text.length());
		assert channel != null;
		assert text != null;

		SendMessage request = new SendMessage(channel, text);
		BaseResponse res = bot.execute(request);

	}

	/**
	 * Sends a message to the specified telegram messenger channel
	 */
	@Override
	public void sendMessageToChannel(ResponseMessage response) {

		assert response != null : "response parameter is null";
		assert response.getChannel() != null : "response has no channel";

		if (response.hasMessage()) {

			if (response.getMessage().length() > 4090)
				response.setMessage(response.getMessage().substring(0, 4089));

			boolean isOK = this.sendFormattedMessageToChannel(response);
			if (!isOK) {
				boolean isOK2 = this.sendFormatted2(response);
				if (!isOK2)
					sendMessageToChannel(response.getChannel(), response.getMessage());
			}
		}

		if (response.hasFile())
			sendFileToChannel(response);

	}

	private boolean sendFormatted2(ResponseMessage response) {

		String channel = response.getChannel();
		String text = response.getMessage();
		if (text.contains("_"))
			text = text.replace("_", "\\_");

		SendMessage request = new SendMessage(channel, text);
		request.parseMode(ParseMode.Markdown);
		request.replyMarkup(new ReplyKeyboardRemove());

		BaseResponse res = bot.execute(request);
		if (!res.isOk())
			System.out.println("failed telegram request: " + res.errorCode() + " " + res.description());
		return res.isOk();

	}

	/**
	 * Sends a message to telegram messenger channel with Markdown formatting and
	 * optional UI elements.
	 */
	private boolean sendFormattedMessageToChannel(ResponseMessage response) {

		assert response != null : "response parameter is null";
		assert response.getChannel() != null : "response has no channel";
		assert response.getMessage() != null : "response has no text message";

		String channel = response.getChannel();
		String text = response.getMessage();

		System.out.println("send formatted message to telegram channel " + channel + ", size: " + text.length());
		SendMessage request = new SendMessage(channel, text);
		request.parseMode(ParseMode.Markdown);

		if (response.hasButtons()) {
			
			int numButton = response.getButtons().size();
			if (numButton == 1) {
				String[] buttons = new String[1];
				buttons[0] = response.getButtons().get(0);
				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(buttons);
				keyboard.oneTimeKeyboard(true);
				request.replyMarkup(keyboard);

			} else if (numButton == 2) {
				String[] buttons = new String[2];
				buttons[0] = response.getButtons().get(0);
				buttons[1] = response.getButtons().get(1);
				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(buttons);
				keyboard.oneTimeKeyboard(true);
				request.replyMarkup(keyboard);

			} else {

				try {

					int length = response.getButtons().get(0).length();
					int rowSize = 3;
					if (length > 20)
						rowSize = 2;
					if (numButton % 2 == 0)
						rowSize = 2;
					if (length > 40)
						rowSize = 1;

					int columns = (int) (Math.ceil(numButton / rowSize));
					if ((numButton % 2 != 0 && numButton % 3 != 0))
						columns = columns + 1;

					String[][] buttons = new String[columns][rowSize];
					int i = 0;
					int j = 0;

					for (String value : response.getButtons()) {
						assert i < columns : "i out of bound";
						assert j < rowSize : "j out of bound";
						buttons[i][j] = value;

						if (j == rowSize - 1) {
							i = i + 1;
							j = 0;
						} else {
							j++;
						}
					}

					for (int row = 0; row < buttons.length; row++) {
						for (int col = 0; col < buttons[row].length; col++) {
							if (buttons[row][col] == null)
								buttons[row][col] = "";
						}
					}
					
					boolean resize = false;
					boolean oneTime = true;
					boolean selective = true;
					
					ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(buttons, resize, oneTime, selective);
					request.replyMarkup(keyboard);
					

				} catch (Exception e) {
					e.printStackTrace();
					request.replyMarkup(new ReplyKeyboardRemove());
				}

			}

		} else
			request.replyMarkup(new ReplyKeyboardRemove());

		BaseResponse res = bot.execute(request);
		if (!res.isOk())
			System.out.println("failed telegram request: " + res.errorCode() + " " + res.description());
		return res.isOk();

	}

	/**
	 * Sends a file to an telegram channel
	 */
	private void sendFileToChannel(ResponseMessage response) {

		assert response != null : "resposne is null";
		assert response.getFile() != null : "response has no file";
		assert response.getChannel() != null;

		String channel = response.getChannel();
		String data = response.getFile().getDataString();
		String name = response.getFile().getName();
		String caption = "";
		System.out.println("Send File to Telegram channel: " + channel);

		try {

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(data);
			data = gson.toJson(je);
			System.out.println(data.substring(0, 160));

		} catch (Exception e) {
			e.printStackTrace();
		}

		byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
		SendDocument request = new SendDocument(channel, bytes);
		
		if (caption != null && !caption.contentEquals(""))
			request.caption(caption);
		if (name != null && !name.contentEquals(""))
			request.fileName(name);

		request.replyMarkup(new ReplyKeyboardRemove());
		BaseResponse res = bot.execute(request);
		System.out.println(String.valueOf(res.isOk()) + " " + res.errorCode() + " " + res.description());
	}

	private MessageFile getFile(String fileId) {

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

		MessageFile res = new MessageFile();
		res.setData(data);
		return res;
	}

	/**
	 * Shows an indication to the user about what the next bots action is
	 * 
	 * @param channel id of channel indication should be shown
	 * @return request was successful (true) or failed (false)
	 */
	public boolean showAction(String channel, ChatAction action) {

		SendChatAction typingAction = new SendChatAction(channel, action);
		BaseResponse response = this.bot.execute(typingAction);
		return response.isOk();
	}

}
