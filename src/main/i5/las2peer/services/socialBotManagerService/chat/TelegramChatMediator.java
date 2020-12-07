package i5.las2peer.services.socialBotManagerService.chat;

import java.nio.charset.StandardCharsets;
import java.util.OptionalLong;

import javax.ws.rs.core.MediaType;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
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
	private final static String url = "https://bcf40f104e84.ngrok.io";
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
			String channel = chat.getAsString("id");
			String user = from.getAsString("first_name");
			String text = message.getAsString("text");
			String timestamp = message.getAsString("date");

			if (channel == null || user == null || text == null || timestamp == null)
				throw new InvalidChatMessageException("missing message fields");

			this.showAction(channel, ChatAction.typing);
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

		if (response.getMessage() != null) {
			boolean isOK = this.sendFormattedMessageToChannel(response);
			if (!isOK)
				sendMessageToChannel(response.getChannel(), response.getMessage());
		}

		if (response.getFile() != null) {
			sendFileToChannel(response);
		}

	}

	/**
	 * Sends a message to telegram messenger channel with Markdown formatting and
	 * optional UI elements.
	 */
	public boolean sendFormattedMessageToChannel(ResponseMessage response) {

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
				request.replyMarkup(keyboard);

			} else if (numButton == 2){
				String[] buttons = new String[2];
				buttons[0] = response.getButtons().get(0);
				buttons[1] = response.getButtons().get(1);
				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(buttons);
				request.replyMarkup(keyboard);
				
			} else {

				int length = response.getButtons().get(0).length();
				int rowSize = 3;
				if (length > 20)
					rowSize = 2;
				if (length > 40)
					rowSize = 1;

				int columns = (int) (Math.ceil(numButton / rowSize));
				String[][] buttons = new String[columns][rowSize];
				int i = 0;
				int j = 0;

				for (String value : response.getButtons()) {
					buttons[i][j] = value;

					if (j == rowSize - 1) {
						i = i + 1;
						j = 0;
					} else {
						j++;
					}
				}

				boolean resize = false;
				boolean oneTime = true;
				boolean selective = true;

				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(buttons, resize, oneTime, selective);
				request.replyMarkup(keyboard);

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
	public void sendFileToChannel(ResponseMessage response) {

		assert response != null : "resposne is null";
		assert response.getFile() != null : "response has no file";
		assert response.getChannel() != null;

		String channel = response.getChannel();
		String data = response.getFile().getDataString();
		String name = response.getFile().getName();
		String caption = "";

		byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
		TelegramBot bot = new TelegramBot(authToken);
		SendDocument request = new SendDocument(channel, bytes);

		if (caption != null)
			request.caption(caption);
		if (name != null)
			request.fileName(name);

		BaseResponse res = bot.execute(request);
		System.out.println(res.description());
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
