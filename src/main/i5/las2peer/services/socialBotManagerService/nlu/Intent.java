package i5.las2peer.services.socialBotManagerService.nlu;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class Intent {
	private String keyword;
	// TODO: Entities
	private float confidence;

	
	public Intent(JSONObject json) {
		JSONObject intentInner = (JSONObject) json.get("intent");
		String confidenceString = intentInner.getAsString("confidence");
		float confidence = Float.parseFloat(confidenceString);
		
		this.keyword = intentInner.getAsString("name");
		this.confidence = confidence;
	}
	
	public String getKeyword() {
		return this.keyword;
	}

	public float getConfidence() {
		return this.confidence;
	}
}
