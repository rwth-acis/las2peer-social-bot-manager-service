package i5.las2peer.services.socialBotManagerService.nlu;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringEscapeUtils;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class RasaNlu extends LanguageUnderstander {
	private String url;
    private String[] Intents; 
	public RasaNlu(String url) {
		this.url = url;
        this.Intents = new String[0];
	}

    public void setIntents(String[] Intents){
        this.Intents = Intents;
    }

    public String[] getIntents(){
        return this.Intents;
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
}
