package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import net.minidev.json.JSONObject;

public class TrainingHelper implements Runnable {
	String url;
	String config;
	String markdownTrainingData;

	boolean success = false;
	private static String[][] UMLAUT_REPLACEMENTS = { { new String("Ä"), "Ae" }, { new String("Ü"), "Ue" },
			{ new String("Ö"), "Oe" }, { new String("ä"), "ae" }, { new String("ü"), "ue" }, { new String("ö"), "oe" },
			{ new String("ß"), "ss" } };

	public static String replaceUmlaute(String orig) {
		String result = orig;

		for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
			result = result.replace(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
		}

		return result;
	}

	public TrainingHelper(String url, String config, String markdownTrainingData) {
		this.url = url;
		this.config = config;
		this.markdownTrainingData = replaceUmlaute(markdownTrainingData);
	}

	@Override
	// Trains and loads the model trained with the data given in the constructor.
	public void run() {
		MiniClient client = new MiniClient();
		client.setConnectorEndpoint(url);

		JSONObject json = new JSONObject();
		json.put("config", config);
		json.put("nlu", markdownTrainingData);

		HashMap<String, String> headers = new HashMap<String, String>();
		ClientResponse response = client.sendRequest("POST", "model/train", json.toJSONString(),
				MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_JSON + ";charset=utf-8", headers);

		String filename = response.getHeader("filename");
		if (filename == null) {
			this.success = false;
			return;
		}

		json = new JSONObject();
		json.put("model_file", "models/" + filename);

		response = client.sendRequest("PUT", "model", json.toString(), MediaType.APPLICATION_JSON + ";charset=utf-8",
				MediaType.APPLICATION_JSON + ";charset=utf-8", headers);
		this.success = response.getHttpCode() == 204;
	}

	public boolean getSuccess() {
		return this.success;
	}
}
