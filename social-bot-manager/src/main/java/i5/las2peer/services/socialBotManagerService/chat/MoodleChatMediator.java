package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Optional;
import java.util.Vector;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;

public class MoodleChatMediator extends ChatMediator {
	private static final String domainName = "https://moodle.tech4comp.dbis.rwth-aachen.de";
	private static String botId;
	private long lastUpdated;
	
	
	public MoodleChatMediator(String authToken) throws AuthTokenException{
		super(authToken);
		
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Instant instant = timestamp.toInstant();
		lastUpdated = instant.getEpochSecond();
		botId = getBotId();
	}

	protected String getBotId() throws AuthTokenException{
		HashMap<String,String> args = new HashMap<String,String>();
		try {
			String response = sendRequest(domainName, "core_webservice_get_site_info", args);
			if(response.contains("Invalid token")){
				throw new AuthTokenException("Authentication Token is faulty!");
			}
			JSONObject json = new JSONObject(response);
			return Integer.toString(json.getInt("userid"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public Boolean sendMessageToChannel(String channel, String text, HashMap<String, IncomingMessage> hashMap, String type, IncomingMessage currentMessage, Optional<String> id) {
		HashMap<String,String> args = new HashMap<String,String>();
		args.put("messages[0][touserid]", channel);
		args.put("messages[0][text]", text);
		Boolean messageSent = Boolean.FALSE;
		try {
			sendRequest(domainName, "core_message_send_instant_messages", args);
			messageSent = Boolean.TRUE;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return messageSent;
	}

	@Override
	public void sendFileMessageToChannel(String channel, File f, String text, Optional<String> id) {
		// TODO Auto-generated method stub

	}

	@Override
	public Vector<ChatMessage> getMessages() {
		Vector<ChatMessage> messages = new Vector<ChatMessage>();
		// Request messages received by the bot
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("useridto", botId);
		args.put("read", "0");
		String response = "";
		try {
			response = sendRequest(domainName, "core_message_get_messages", args);
			
			// Extract relevant information and create message objects for recent messages
			JSONObject json = new JSONObject(response);
			JSONArray receivedMessages = (JSONArray) json.get("messages");
			long last = 0; // Timestamp of the last message
			for (int i = 0; i < receivedMessages.length(); i++) {
				JSONObject messageObj = (JSONObject) receivedMessages.get(i);
				String userid = Integer.toString(messageObj.getInt("useridfrom"));
				String text = messageObj.getString("fullmessage");
				long timecreated = messageObj.getLong("timecreated");
				if (lastUpdated < timecreated) {
					ChatMessage message = new ChatMessage(userid, userid, text);
					messages.add(message);
					if (last < timecreated) {
						last = timecreated;
					}
				}
			}
			if (last != 0) {
				lastUpdated = last;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	@Override
	public String getChannelByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

	@Override
	public void sendFileMessageToChannel(String channel, String fileBody, String fileName, String fileType,
			String text, Optional<String> id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendBlocksMessageToChannel(String channel, String blocks, String authToken, HashMap<String, IncomingMessage> hashMap, Optional<String> id) {

	}

	@Override
	public void sendBlocksMessageToChannel(String channel, String blocks, String authToken) {
		super.sendBlocksMessageToChannel(channel, blocks, authToken);
	}

	@Override
	public void updateBlocksMessageToChannel(String channel, String blocks, String authToken, String ts, Optional<String> id){

	}

	@Override
	public void updateBlocksMessageToChannel(String channel, String blocks, String authToken, String ts) {
		super.updateBlocksMessageToChannel(channel, blocks, authToken, ts);
	}


	@Override
	public void editMessage(String channel, String messageId, String message, Optional<String> id){}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
