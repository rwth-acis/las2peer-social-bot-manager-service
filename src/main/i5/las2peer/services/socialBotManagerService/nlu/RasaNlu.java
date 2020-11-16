package i5.las2peer.services.socialBotManagerService.nlu;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringEscapeUtils;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class RasaNlu extends LanguageUnderstander {
	private Collection<String> Intents;

	public RasaNlu(String url) {
		this.url = url;
		this.name = "Rasa";
		this.Intents = new HashSet<String>();
		
		String prefix = "https://";
		if(url.startsWith(prefix))
			this.name = "Rasa " + url.substring(prefix.length());
		prefix = "http://";
		if(url.startsWith(prefix))
			this.name = "Rasa " + url.substring(prefix.length());
	}

	public void setIntents(Collection<String> Intents) {
		this.Intents = Intents;
	}

	public void addIntent(String intent) {
		this.Intents.add(intent);
	}
	
	@Override
	public void addIntents(Collection<String> intents) {
		this.Intents.addAll(intents);	
	}

	@Override
	public Collection<String> getIntents() {
		return this.Intents;
	}

	@Override
	public Intent getIntent(String input) {
		JSONObject intentJSON;
		try {
			intentJSON = getIntentJSON(input);
			return new Intent(intentJSON);
		} catch (IOException | ParseException e) {
			System.err.println("Error retrieving intent from Rasa NLU:");
			e.printStackTrace();
		}
		return null;
	}

	private JSONObject getIntentJSON(String input) throws IOException, ParseException {
		try {
			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(this.url);
			System.out.println("Rasa url:" + this.url);
			JSONObject inputJSON = new JSONObject(
					Collections.singletonMap("text", StringEscapeUtils.escapeJson(input)));
			HashMap<String, String> headers = new HashMap<String, String>();
			ClientResponse response = client.sendRequest("POST", "model/parse", inputJSON.toString(),
					MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, headers);
			System.out.println("Result: " + response.getResponse());
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			return (JSONObject) p.parse(response.getResponse());
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONObject();
		}
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
