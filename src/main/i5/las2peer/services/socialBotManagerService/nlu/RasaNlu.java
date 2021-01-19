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
import i5.las2peer.services.socialBotManagerService.parser.training.TrainingData;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class RasaNlu extends LanguageUnderstander {
	private Collection<String> Intents;
	private TrainingData data;
	
	public RasaNlu(String url) {
		this.url = url;
		this.name = "Rasa";
		this.Intents = new HashSet<String>();

		String prefix = "https://";
		if (url.startsWith(prefix))
			this.name = "Rasa " + url.substring(prefix.length());
		prefix = "http://";
		if (url.startsWith(prefix))
			this.name = "Rasa " + url.substring(prefix.length());
	}

	public void setIntents(Collection<String> Intents) {
		this.Intents = Intents;
	}

	@Override
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

		try {
			
			String intentJSONString = requestParsing(input);
			if (intentJSONString == null)
				return null;
			
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject intentJSONObject = (JSONObject) parser.parse(intentJSONString);
			
			return new Intent(intentJSONObject);
			
		} catch (IOException | ParseException e) {
			System.err.println("Error parsing received intent from Rasa NLU:");
			e.printStackTrace();
		}
		
		return null;
	}

	private String requestParsing(String jsonInput) throws IOException, ParseException {

		assert jsonInput != null;
		invariant();
		System.out.println("Requesting Rasa server: " + this.url);

		ClientResponse response = null;
		try {

			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(this.url);

			JSONObject inputJSON = new JSONObject(
					Collections.singletonMap("text", StringEscapeUtils.escapeJson(jsonInput)));
			HashMap<String, String> headers = new HashMap<String, String>();
			response = client.sendRequest("POST", "model/parse", inputJSON.toString(), MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON, headers);

			if (response != null)
				return response.getResponse();

		} catch (Exception e) {
			System.err.println("Error retrieving intent from Rasa NLU:");
			e.printStackTrace();
		}

		return null;
	}

	protected void invariant() {
		assert this.url != null : "Rasa NLU has no url specified";
		assert this.url.startsWith("http") : "Rasa NLU url is not a valid url";
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void addTrainingData(TrainingData data) {
		assert data != null;
				
		if(this.data == null) {
			this.data = data;
		} else {
			this.data.addAll(data);
		}
		
	}

	@Override
	public TrainingData getTrainingData() {
		return this.data;
	}

}
