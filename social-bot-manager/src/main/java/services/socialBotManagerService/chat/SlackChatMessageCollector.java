// package services.socialBotManagerService.chat;

// import com.slack.api.rtm.*;

// import net.minidev.json.JSONObject;
// import net.minidev.json.parser.JSONParser;
// import net.minidev.json.parser.ParseException;

// public class SlackChatMessageCollector extends ChatMessageCollector implements RTMMessageHandler {
// 	public void handle(String messageJsonString) {
// 		System.out.println("Received message: " + messageJsonString);
// 		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
// 		JSONObject messageJson;
// 		try {
// 			messageJson = (JSONObject) p.parse(messageJsonString);
// 			String type = messageJson.getAsString("type");
// 			System.out.println("Type Check");
// 			if (type != null) {
// 				if (type.equals("message")) {
// 					ChatMessage message;
// 					message = SlackChatMediator.parseSlackMessage(messageJson);
// 					if (SlackChatMediator.usersByChannel.get(message.getChannel()) != null) {
// 						message.setEmail(SlackChatMediator.usersByChannel.get(message.getChannel()));
// 					} else {
// 						message.setEmail(SlackChatMediator.fetchEmailByUserId(message.getUser()));
// 					}
// 					System.out.println("message: " + message);
// 					// If bot sent file to user, don't add message
// 					if (!SlackChatMediator.botIDs.contains(messageJson.get("user"))) {
// 						this.addMessage(message);
// 					}
// 				} else if (type.equals("goodbye")) {
// 					System.out.println("Slack client disconnected");
// 					this.setConnected(false);

// 				}
// 			} else {
// 				System.out.println("Skipped");
// 			}
// 		} catch (ParseException | InvalidChatMessageException e) {
// 			// TODO We may want to handle other types of messages as well.
// 			System.out.println("Invalid message skipped");
// 		}
// 	}
// }
