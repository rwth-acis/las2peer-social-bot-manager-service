package i5.las2peer.services.socialBotManagerService.nlu;

import net.minidev.json.JSONObject;

public class Entity {
	private String entityName;
	private String value;
	private float confidence;
	
	public Entity(JSONObject o) {
		this.entityName = o.getAsString("entity");
		this.value = o.getAsString("value");
		this.confidence = o.getAsNumber("confidence").floatValue();
	}
	
	public String getEntityName() {
		return entityName;
	}

	public float getConfidence() {
		return confidence;
	}

	public String getValue() {
		return value;
	}
}
