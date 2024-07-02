package services.socialBotManagerService.nlu;

import net.minidev.json.JSONObject;

public class Entity {
	private String entityName;
	private String value;
	private float confidence;

	// Constructor for entity extraction through Rasa NLU.
	public Entity(JSONObject o) {
		this.entityName = o.getAsString("entity");
		this.value = o.getAsString("value");
		if (o.getAsNumber("confidence") == null) {
			// added this because there was a nullpointerexception when extracting entities,
			// may need to further search what caused the problem
			// System.out.println("No Confidence Available");
			this.confidence = 1;
		} else {
			if (o.getAsNumber("confidence_entity") != null) {
				this.confidence = o.getAsNumber("confidence_entity").floatValue();
			} else
				this.confidence = o.getAsNumber("confidence").floatValue();
		}
		System.out.println("Entityname is" + this.entityName);
		System.out.println("Entityvalue is" + this.value);
	}

	// Constructor for bypassing entity extraction. Used for '!'-commands, for
	// example.
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
