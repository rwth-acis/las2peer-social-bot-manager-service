package i5.las2peer.services.socialBotManagerService.nlu;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class RasaNlu {
	private URL url;
	
	// TODO: Handle non-default model
	public RasaNlu(URL url) {
		this.url = url;
	}

	public JSONObject getIntentJSON(String input) throws IOException, ParseException {
		MiniClient client = new MiniClient();
		client.setConnectorEndpoint(this.url.toString());
		JSONObject inputJSON = 
				new JSONObject(Collections.singletonMap("text", input));
		ClientResponse response =
				client.sendRequest("POST", "model/parse", inputJSON.toString());
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		return (JSONObject)p.parse(response.getResponse());
	}
	
	public Intent getIntent(String input) throws IOException, ParseException {
		JSONObject intentJSON = getIntentJSON(input);
		return new Intent(intentJSON);
	}
}
