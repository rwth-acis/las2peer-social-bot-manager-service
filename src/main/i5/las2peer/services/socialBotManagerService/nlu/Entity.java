package i5.las2peer.services.socialBotManagerService.nlu;

import net.minidev.json.JSONObject;

public class Entity {
	private String entityName;
	private String value;
	private float confidence;

	// Constructor for entity extraction through Rasa NLU.
	public Entity(JSONObject o) {
		this.entityName = o.getAsString("entity");
		this.value = o.getAsString("value");
		this.confidence = o.getAsNumber("confidence_entity").floatValue();
	}

	// Constructor for bypassing entity extraction. Used for '!'-commands, for example.
	public Entity(String entityName, String entityValue) {
		this.entityName = entityName;
		this.value = entityValue;
		this.confidence = 1.0f;
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
