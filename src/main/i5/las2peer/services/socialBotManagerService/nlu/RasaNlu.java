package i5.las2peer.services.socialBotManagerService.nlu;

import java.io.IOException;
import java.util.Collections;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class RasaNlu {
	private String url;

	// TODO: Handle non-default model
	public RasaNlu(String url) {
		this.url = url;
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

	public void train(String modelName, String modelMarkdown) {
		// TODO: Use the /model/train POST method to train a Rasa NLU model
		// - Build a config file with "language: en, pipeline: spacy_sklearn" or similar
		// - Send the config file and the Markdown data to the endpoint, setting "output" to `modelName`
		// - Response is the file name of the trained model, load it
	}

	private JSONObject getIntentJSON(String input) throws IOException, ParseException {
		MiniClient client = new MiniClient();
		client.setConnectorEndpoint(this.url);
		JSONObject inputJSON =
				new JSONObject(Collections.singletonMap("text", input));
		ClientResponse response =
				client.sendRequest("POST", "model/parse", inputJSON.toString());
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		return (JSONObject)p.parse(response.getResponse());
	}
}
