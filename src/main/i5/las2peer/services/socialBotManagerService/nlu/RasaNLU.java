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

public class RasaNLU extends LanguageUnderstander {

	private String name;
	private String url;
	private Collection<String> Intents;
	private TrainingData data;

	public RasaNLU(String url) {
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

	public RasaNLU(String name, String string) {
		this(string);
		this.name = name;
	}

	public void setIntents(Collection<String> Intents) {
		this.Intents = Intents;
	}

	public void addIntent(String intent) {
		this.Intents.add(intent);
	}

	public void addIntents(Collection<String> intents) {
		this.Intents.addAll(intents);
	}

	@Override
	public Collection<String> getIntents() {
		return this.Intents;
	}

	public Intent getIntent(String input) {
		return this.parse(input);
	}

	@Override
	public Intent parse(String input) {

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

	@Override
	public TrainingData getTrainingData() {
		return this.data;
	}

	@Override
	public void addTrainingData(TrainingData data) {
		assert data != null;

		if (this.data == null) {
			this.data = data;
		} else {
			this.data.addAll(data);
		}
	}

	@Override
	public void addTrainingData(TrainingDataEntry entry) {

		if (entry == null)
			return;

		if (this.data == null)
			this.data = new TrainingData();

		this.data.addEntry(entry);
	}

	@Override
	protected void setTrainingData(TrainingData data) {
		assert data != null;
		this.data = data;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getUrl() {
		return this.url;
	}

	protected void invariant() {
		assert this.url != null : "Rasa NLU has no url specified";
		assert this.url.startsWith("http") : "Rasa NLU url is not a valid url";
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
