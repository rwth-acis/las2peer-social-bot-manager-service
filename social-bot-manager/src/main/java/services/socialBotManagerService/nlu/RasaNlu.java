package services.socialBotManagerService.nlu;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.StringEscapeUtils;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class RasaNlu {
	private String url;
    private HashSet<String> intents; 
	public RasaNlu(String url) {
		this.url = url;
        this.intents = new HashSet<String>();
	}

	public void addIntent(String intent){
		this.intents.add(intent); 
	}

    public void setIntents(HashSet<String> intents){
        this.intents = intents;
    }

    public String[] getIntents(){
        return this.intents.toArray(new String[this.intents.size()]);
    }
    
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
			// MiniClient client = new MiniClient();
			// client.setConnectorEndpoint(this.url);
			JSONObject inputJSON = new JSONObject(
					Collections.singletonMap("text", StringEscapeUtils.escapeJson(input)));
			HashMap<String, String> headers = new HashMap<String, String>();
			// ClientResponse response = client.sendRequest("POST", "model/parse", inputJSON.toString(),
			// 		MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, headers);
			String url = this.url + "/model/parse";
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(new URI(url))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(inputJSON.toJSONString()))
				.build();
			HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			return (JSONObject) p.parse(response.body());
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONObject();
		}
	}
}
