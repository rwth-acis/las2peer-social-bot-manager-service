package i5.las2peer.services.socialBotManagerService.chat;

import com.github.seratch.jslack.api.rtm.*;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class SlackChatMessageCollector extends ChatMessageCollector implements RTMMessageHandler {
	public void handle(String messageJsonString) {
		System.out.println("Received message: " + messageJsonString);
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);

		JSONObject messageJson;
		try {
			messageJson = (JSONObject) p.parse(messageJsonString);
			String type = messageJson.getAsString("type");
			
			if (type != null && type.equals("message")) {
				ChatMessage message;
				message = SlackChatMediator.parseSlackMessage(messageJson);
				this.addMessage(message);
			} else {
				System.out.println("Skipped");
			}
		} catch (ParseException | InvalidChatMessageException e) {
			// TODO We may want to handle other types of messages as well.
			System.out.println("Invalid message skipped");
		}
	}
}
