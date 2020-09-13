package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.OptionalLong;

import javax.ws.rs.core.MediaType;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
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
	
	try {

	    JSONObject message = (JSONObject) event.get("message");
	    JSONObject chat = (JSONObject) message.get("chat");
	    JSONObject from = (JSONObject) message.get("from");
	    String channel = chat.getAsString("id");
	    String user = from.getAsString("first_name");
	    String text = message.getAsString("text");
	    String timestamp = message.getAsString("date");

	    if (channel == null || user == null || text == null || timestamp == null) {
		throw new InvalidChatMessageException("missing message fields");
	    }
	    
	    ChatMessage chatMessage = new ChatMessage(channel, user, text, timestamp);
	    //this.addMessage(chatMessage);
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

	String url = "https://3180d63348f9.ngrok.io";
	ClientResponse result = client.sendRequest("GET",
		"setWebhook?url=" + url + "/sbfmanager/bots/events/telegram/" + super.authToken, MediaType.TEXT_PLAIN);
	System.out.println(result.getResponse());
    }

    @Override
    public void sendMessageToChannel(String channel, String text, OptionalLong id) {

	System.out.println("send telegram message: " + text);
	String encoded = "";
	try {
	    encoded = URLEncoder.encode(text, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	ClientResponse result = client.sendRequest("POST", "sendmessage?text=" + encoded + "&chat_id=" + channel,
		MediaType.TEXT_PLAIN);
    }

    @Override
    public void sendFileMessageToChannel(String channel, File f, String text, OptionalLong id) {
	// TODO Auto-generated method stub

    }

    public boolean hasToken(String token) {
	return (this.authToken.equals(token));
    }

}
