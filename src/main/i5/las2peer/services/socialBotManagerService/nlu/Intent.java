package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.HashMap;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class Intent {
	private String intentKeyword;
	private float confidence;

	private HashMap<String, Entity> entities;

	public Intent(JSONObject json) {
		JSONObject intentInner = (JSONObject) json.get("intent");
		float confidence = intentInner.getAsNumber("confidence").floatValue();
		this.intentKeyword = intentInner.getAsString("name");
		this.confidence = confidence;

		JSONArray entities = (JSONArray) json.get("entities");
		HashMap<String, Entity> entitiesMap = new HashMap<String, Entity>();
		entities.forEach(
			o ->
			{
				Entity entity = new Entity((JSONObject)o);
				entitiesMap.put(entity.getEntityName(), entity);
			}
		);
		this.entities = entitiesMap;
	}

	public String getKeyword() {
		return this.intentKeyword;
	}

	public float getConfidence() {
		return this.confidence;
	}

	public Entity getEntity(String entity) {
		return this.entities.get(entity);
	}
}
