package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.OptionalLong;
import java.util.Random;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import net.minidev.json.JSONObject;

public class TelegramChatMediator extends EventChatMediator {

    MiniClient client;

    public TelegramChatMediator(String authToken) {
	super(authToken);

	this.client = new MiniClient();
	client.setConnectorEndpoint("https://api.telegram.org/bot" + authToken);

	this.settingWebhook();
    }

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
     */
    public void settingWebhook() {

	String url = "https://14e66b4494af.ngrok.io";
	ClientResponse result = client.sendRequest("GET",
		"setWebhook?url=" + url + "/sbfmanager/bots/events/telegram/" + super.authToken, MediaType.TEXT_PLAIN);
	System.out.println(result.getResponse());
    }

    @Override
    public void sendMessageToChannel(String channel, String text, OptionalLong id) {

	System.out.println("send telegram message: " + text);
	String encoded = "";
	try {
	    text = text.replace("_", "\\_");
	    encoded = URLEncoder.encode(text, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	ClientResponse result = client.sendRequest("POST",
		"sendmessage?text=" + encoded + "&chat_id=" + channel + "&parse_mode=Markdown", MediaType.TEXT_PLAIN);

	System.out.println(result.getResponse());

    }

    @Override
    public void sendMessageToChannel(String channel, ResponseMessage response) {

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

    public long storeFile(String file) {

	long id = new Random().nextLong();
	if (id < 0)
	    id = -id;
	int parts = 1;
	String name = "fileName";

	byte[] bytes = file.getBytes();

	int counter = 0;
	int partSize = 131072;
	if(bytes.length <= partSize) {
	    
	    String file_id = "file_id=" + String.valueOf(id);
	    String file_part = "&file_part=" + String.valueOf(0);
	    String file_byte = "&bytes=" + bytes;

	    ClientResponse result = client.sendRequest("POST",
		    "saveFilePart?" + file_id + file_part + file_byte,
		    MediaType.TEXT_PLAIN);
	    System.out.println(result.getResponse());
	} else {
	    System.out.println("large file");
	}
	return id;

    }

    @Override
    public void sendFileToChannel(String channel, ResponseMessage response) {

	assert response != null : "resposne is null";
	assert response.getFile() != null : "response has no file";

	String file = response.getFile().getData();
	long id = storeFile(file);
	
	String uniqueID = UUID.randomUUID().toString();
	String ft = "&document={\"file_id\":" + String.valueOf(id) + "\",\"file_unique_id\":\"" + uniqueID + "\"";
	String chat = "&chat_id=" + channel;

	ClientResponse result = client.sendRequest("POST", "sendmessage?text=file" + chat + ft,
		MediaType.TEXT_PLAIN);

	System.out.println(result.getResponse());
    }

    @Override
    public void sendFileMessageToChannel(String channel, File f, String text, OptionalLong id) {
	// TODO Auto-generated method stub

    }

    public boolean hasToken(String token) {
	return (this.authToken.equals(token));
    }


}
