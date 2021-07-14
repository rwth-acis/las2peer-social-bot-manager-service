package i5.las2peer.services.socialBotManagerService.chat;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public abstract class ChatMediator {
	protected String authToken;

	public ChatMediator(String authToken) {
		this.authToken = authToken;
	}

	/**
	 * Sends a chat message to a channel.
	 *
	 * @param channel A channel ID valid for interacting with the chat service's API
	 * @param text The content of the chat message
	 * @param id An ID for the sent chat message, e.g. to be able to recognize replies to it later on.
	 */
	public abstract void sendMessageToChannel(String channel, String text, Optional<String> id);

	//Leo: commented this out because it still isn't used elsewhere

	public abstract void sendAttachmentMessageToChannel(String channel, String attachments, Optional<String> id);

	public void sendAttachmentMessageToChannel(String channel, String attachments) {
		sendAttachmentMessageToChannel(channel, attachments, Optional.empty());
	}

	public abstract void sendBlocksMessageToChannel(String channel, String blocks, Optional<String> id);

	public void sendBlocksMessageToChannel(String channel, String blocks) {
		sendBlocksMessageToChannel(channel, blocks, Optional.empty());
	}

	/**
	 * Sends a chat message to a channel.
	 *
	 * @param channel A channel ID valid for interacting with the chat service's API
	 * @param text The content of the chat message
	 */
	public void sendMessageToChannel(String channel, String text) {
		sendMessageToChannel(channel, text, Optional.empty());
	}

	/**
	 * Sends a file message to a channel as well as an optional text message.
	 *
	 * @param channel A channel ID valid for interacting with the chat service's API
	 * @param f A file object
	 * @param text Text to be sent with file
	 * @param id An ID for the sent chat message, e.g. to be able to recognize replies to it later on.
	 */
	public abstract void sendFileMessageToChannel(String channel, File f, String text, Optional<String> id);

	/**
	 * Sends a file message to a channel as well as an optional text message.
	 *
	 * @param channel A channel ID valid for interacting with the chat service's API
	 * @param f A file object
	 * @param text Text to be sent with file
	 */
	public void sendFileMessageToChannel(String channel, File f, String text) {
		sendFileMessageToChannel(channel, f, text, Optional.empty());
	}

	/**
	 * Sends a file message to a channel as well as an optional text message and takes care of converting base64 to a
	 * File object.
	 *
	 * @param channel A channel ID valid for interacting with the chat service's API
	 * @param fileBody Body of the file to be generated
	 * @param fileName Name of the file to be generated
	 * @param fileType Type of the file to be generated
	 * @param text Text to be sent with file
	 * @param id  An ID for the sent chat message, e.g. to be able to recognize replies to it later on.
	 */
	public void sendFileMessageToChannel(String channel, String fileBody, String fileName, String fileType,
												  String text, Optional<String> id) {
		byte[] decodedBytes = java.util.Base64.getDecoder().decode(fileBody);
		File file = new File(fileName + "." + fileType);
		try {
			FileUtils.writeByteArrayToFile(file, decodedBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendFileMessageToChannel(channel, file, text, id);
	};

	/**
	 * Sends a file message to a channel as well as an optional text message and takes care of converting base64 to a
	 * File object.
	 *
	 * @param channel A channel ID valid for interacting with the chat service's API
	 * @param fileBody Body of the file to be generated
	 * @param fileName Name of the file to be generated
	 * @param fileType Type of the file to be generated
	 * @param text Text to be sent with file
	 */
	public void sendFileMessageToChannel(String channel, String fileBody, String fileName, String text,
										 String fileType) {
		sendFileMessageToChannel(channel, fileBody, fileName, fileType, text, Optional.empty());
	}

	/**
	 * Gets messages the mediator received since the last time the method was called.
	 *
	 * @return A Vector containing the ChatMessages received since the last time the method was called.
	 */
	public abstract Vector<ChatMessage> getMessages();

	/**
	 * Gets the IM channel ID for the user registered under the given E-Mail address.
	 *
	 * @param email The E-Mail address of the user to query
	 * @return If user was found, their IM channel ID, null otherwise.
	 */
	public abstract String getChannelByEmail(String email);

	public Boolean hasToken(String token) {
		return (this.authToken.equals(token));
	}
	
	protected String sendRequest(String domainName, String function, HashMap<String, String> args) throws IOException {
		String url = domainName + "/webservice/rest/server.php" + "?wstoken=" + authToken 
				+ "&moodlewsrestformat=json" + "&wsfunction=" + function + "&" + getDataString(args);
		
		//System.out.println("Debug --- URL: " + url);
		
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setRequestMethod("POST");
		
		InputStream is = con.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuilder response = new StringBuilder();
		while ((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}
		rd.close();
		
		//System.out.println("Debug --- Response: " + response.toString());
		return response.toString();
	}
	
	protected String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (first) {
				first = false;
			} else {
				result.append("&");	
			}
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return result.toString();
	}

	public abstract void close();
}
