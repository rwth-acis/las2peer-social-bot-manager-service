package i5.las2peer.services.socialBotManagerService.chat;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class Slack extends Chat{
	private String endpoint = "https://slack.com/api/";
	private MiniClient client = new MiniClient();
	private Map<String, String> header = new HashMap<String, String>();
	private String token;
	
	public Slack(String token) {
		this.setName("Slack");
		client.setConnectorEndpoint(endpoint);
		this.token = token;
	}
	
	
	public void sendMessage(String botName, String botPassword, String message, String channel) {
		this.client.setLogin(botName, botPassword);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("channel", channel);
		params.put("token", this.token);
		params.put("text", message);
		params.put("username", botName);
		System.out.println(params.toString());
		if(channel!=null) {
			try {
				client.sendRequest("POST", "chat.postMessage", getDataString(params),
					"application/x-www-form-urlencoded", "application/x-www-form-urlencoded", header);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Message sent to "+ channel);
		}else {
			System.out.println("Channel not found");
		}
	}
	
	public void sendMessage(String botName, String botPassword, String message, String channel, String email) {
		JSONArray imList = this.getChannels();
		JSONArray memberList = this.getUsers();
		for (Object o : memberList) {
			if (o instanceof JSONObject) {
				JSONObject jo = (JSONObject) o;
				JSONObject profile = (JSONObject) jo.get("profile");
				if (profile.get("email") != null) {
					String pmail = (String) profile.get("email");
					if (pmail==email) {
						String uId = jo.getAsString("id");
						for (Object q : imList) {
							if (q instanceof JSONObject) {
								JSONObject channels = (JSONObject) q;
								if (channels.get("user") != null) {
									String u = (String) channels.get("user");
									if (u.equals(uId)) {
										channel = channels.getAsString("id");
										this.sendMessage(botName, botPassword, message, channel);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private JSONArray getUsers() {
		HashMap<String, String> params = new HashMap<String, String>();
		ClientResponse r;
		try {
			r = client.sendRequest("POST", "users.list", getDataString(params),
					"application/x-www-form-urlencoded", "application/x-www-form-urlencoded", header);
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject j;
			try {
				j = (JSONObject) p.parse(r.getResponse());
				return (JSONArray) j.get("members");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private JSONArray getChannels() {
		HashMap<String, String> params = new HashMap<String, String>();
		ClientResponse r;
		try {
			r = client.sendRequest("POST", "im.list", getDataString(params),
					"application/x-www-form-urlencoded", "application/x-www-form-urlencoded", header);
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject ims;
			try {
				ims = (JSONObject) p.parse(r.getResponse());
				return (JSONArray) ims.get("ims");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
