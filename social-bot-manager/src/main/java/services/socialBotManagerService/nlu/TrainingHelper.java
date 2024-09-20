package services.socialBotManagerService.nlu;

import java.util.HashMap;

import javax.ws.rs.core.UriBuilder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
		
		JSONObject json = new JSONObject();
		json.put("config", config);
		if (markdownTrainingData.contains("examples: |")) {
		//	json.put("domain", markdownTrainingData.replace("\\t", ""));
			json.put("nlu", markdownTrainingData);
			HashMap<String, String> headers = new HashMap<String, String>();
			
			// Send the request
			try {
				HttpClient httpClient = HttpClient.newHttpClient();
				HttpRequest httpRequest = HttpRequest.newBuilder()
						.uri(UriBuilder.fromUri(url + "/model/train").build())
						.headers(headers.toString())
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(markdownTrainingData))
						.build();

				HttpResponse<String> serviceResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
				String filename = serviceResponse.headers().firstValue("filename").get();

				if (filename == null) {
					this.success = false;
					return;
				}

				json = new JSONObject();
				json.put("model_file", "models/" + filename);

				httpRequest = HttpRequest.newBuilder()
						.uri(UriBuilder.fromUri(url + "/model").build()).headers(headers.toString())
						.header("Content-Type", "application/json")
						.PUT(HttpRequest.BodyPublishers.ofString(json.toString()))
						.build();

				serviceResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
				this.success = serviceResponse.statusCode() == 204;

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

		} else {
			json.put("nlu", markdownTrainingData);
			HashMap<String, String> headers = new HashMap<String, String>();
            try {
				HttpClient httpClient = HttpClient.newHttpClient();
				HttpRequest httpRequest = HttpRequest.newBuilder()
						.uri(UriBuilder.fromUri(url+ "/model/train").build())
						.headers(headers.toString())
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(json.toJSONString()))
						.build();
				HttpResponse<String> serviceResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
				String filename = serviceResponse.headers().firstValue("filename").get();
				if (filename == null) {
					this.success = false;
					return;
				}
		
				json = new JSONObject();
				json.put("model_file", "models/" + filename);
		
				httpRequest = HttpRequest.newBuilder()
				.uri(UriBuilder.fromUri(url + "/model").build()).headers(headers.toString())
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(json.toString()))
				.build();
				serviceResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
				this.success = serviceResponse.statusCode() == 204;
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean getSuccess() {
		return this.success;
	}
}
