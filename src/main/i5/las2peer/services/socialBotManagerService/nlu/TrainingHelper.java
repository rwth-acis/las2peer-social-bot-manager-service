package i5.las2peer.services.socialBotManagerService.nlu;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import net.minidev.json.JSONObject;

public class TrainingHelper implements Runnable {
	String url;
	String config;
	String markdownTrainingData;

	boolean success = false;

	public TrainingHelper(String url, String config, String markdownTrainingData) {
		this.url = url;
		this.config = config;
		this.markdownTrainingData = markdownTrainingData;
	}

	@Override
	// Trains and loads the model trained with the data given in the constructor.
	public void run() {
		MiniClient client = new MiniClient();
		client.setConnectorEndpoint(url);

		JSONObject json = new JSONObject();
		json.put("config", config);
		json.put("nlu", markdownTrainingData);

		ClientResponse response = client.sendRequest("POST", "model/train", json.toString());

		String filename = response.getHeader("filename");
		if (filename == null) {
			this.success = false;
			return;
		}

		json = new JSONObject();
		json.put("model_file", "models/" + filename);

		response = client.sendRequest("PUT", "model", json.toString());
		this.success = response.getHttpCode() == 204;
	}

	public boolean getSuccess() {
		return this.success;
	}
}
