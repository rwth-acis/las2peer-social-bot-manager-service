package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.parser.training.BotMonitorEventBodyGenerator;
import net.minidev.json.JSONObject;

public class RasaTrainer implements Runnable {

	String url;
	String config;
	String markdownTrainingData;
	LanguageUnderstander nlu;
	TrainingData trainingData;
	String botEventId;
	Context context;
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

	public RasaTrainer(Context context, String botEventId, String url, String config, String markdownTrainingData) {
		this.context = context;
		this.botEventId = botEventId;
		this.url = url;
		this.config = config;
		this.markdownTrainingData = replaceUmlaute(markdownTrainingData);
	}

	public RasaTrainer(String url, String config, String markdownTrainingData) {
		this.url = url;
		this.config = config;
		this.markdownTrainingData = replaceUmlaute(markdownTrainingData);
	}

	public RasaTrainer(Context context, String botEventId, LanguageUnderstander nlu, TrainingData trainingData) {

		this.context = context;
		this.botEventId = botEventId;
		this.url = nlu.getUrl();
		this.trainingData = trainingData;
		System.out.println("training data intents size: " + trainingData.getEntries().size());
		this.markdownTrainingData = replaceUmlaute(trainingData.toMarkdown());
		this.setDefaultConfig();
		this.nlu = nlu;

	}

	@Override
	// Trains and loads the model trained with the data given in the constructor.
	public void run() {

		MiniClient client = new MiniClient();
		client.setConnectorEndpoint(url);

		JSONObject json = new JSONObject();
		json.put("config", config);
		json.put("nlu", markdownTrainingData);

		String filename = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		try {

			ClientResponse response = client.sendRequest("POST", "model/train", json.toJSONString(),
					MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_JSON + ";charset=utf-8",
					headers);

			if (response != null && response.getHttpCode() < 300 && response.getHttpCode() >= 200) {
				filename = response.getHeader("filename");
				if (filename != null && !filename.contentEquals("")) {
					success = true;
					System.out.println("training was successful");
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// trigger event
		if (this.botEventId != null) {

			BotMonitorEventBodyGenerator gen = new BotMonitorEventBodyGenerator("sbfmanager");
			String body = null;
			if (success)
				body = gen.eventBody(botEventId, "trainsuccess", null);
			else
				body = gen.eventBody(botEventId, "trainerror", null);
			context.monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_82, body);

		}

		// success, adapt new training data
		if (success) {

			System.out.println("success, adapt new training data");
			nlu.setTrainingData(trainingData);
			json = new JSONObject();
			json.put("model_file", "models/" + filename);

			ClientResponse response = client.sendRequest("PUT", "model", json.toString(),
					MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_JSON + ";charset=utf-8",
					headers);
			this.success = response.getHttpCode() == 204;

		}	

	}

	public boolean getSuccess() {
		return this.success;
	}

	public void setDefaultConfig() {
		this.config = "language: \"en\"\npipeline:\n - name: WhitespaceTokenizer\n - name: RegexFeaturizer\n - name: CRFEntityExtractor\n - name: EntitySynonymMapper\n - name: CountVectorsFeaturizer\n - name: DIETClassifier\npolicies:\n - name: MemoizationPolicy\n - name: KerasPolicy\n - name: MappingPolicy\n - name: FormPolicy\n";
	}
}
