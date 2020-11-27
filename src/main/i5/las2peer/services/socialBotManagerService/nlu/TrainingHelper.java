package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.parser.training.BotMonitorEventBodyGenerator;
import net.minidev.json.JSONObject;

public class TrainingHelper implements Runnable {

	String url;
	String config;
	String markdownTrainingData;

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

	public TrainingHelper(Context context, String botEventId, String url, String config, String markdownTrainingData) {
		this.context = context;
		this.botEventId = botEventId;
		this.url = url;
		this.config = config;
		this.markdownTrainingData = replaceUmlaute(markdownTrainingData);
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

		String filename = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		try {	
			
			ClientResponse response = client.sendRequest("POST", "model/train", json.toJSONString(),
					MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_JSON + ";charset=utf-8", headers);
			
			if(response != null)
				filename = response.getHeader("filename");

		} catch (Exception e) {
			e.printStackTrace();
		}

		if(this.botEventId != null) {
			
			BotMonitorEventBodyGenerator gen = new BotMonitorEventBodyGenerator("sbfmanager");
			String body = null;
			if(filename != null)
				body = gen.eventBody(botEventId, "trainsuccess", null);
			else
				body = gen.eventBody(botEventId, "trainerror", null);
			context.monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_82, body);
		
		}
		
		if(filename == null)
			return;
		
		json = new JSONObject();
		json.put("model_file", "models/" + filename);

		ClientResponse response = client.sendRequest("PUT", "model", json.toString(), MediaType.APPLICATION_JSON + ";charset=utf-8",
				MediaType.APPLICATION_JSON + ";charset=utf-8", headers);
		this.success = response.getHttpCode() == 204;
	
	}

	public boolean getSuccess() {
		return this.success;
	}

	public void setDefaultConfig() {
		this.config = "language: \"en\"\npipeline:\n - name: WhitespaceTokenizer\n - name: RegexFeaturizer\n - name: CRFEntityExtractor\n - name: EntitySynonymMapper\n - name: CountVectorsFeaturizer\n - name: DIETClassifier\npolicies:\n - name: MemoizationPolicy\n - name: KerasPolicy\n - name: MappingPolicy\n - name: FormPolicy\n";
	}
}
