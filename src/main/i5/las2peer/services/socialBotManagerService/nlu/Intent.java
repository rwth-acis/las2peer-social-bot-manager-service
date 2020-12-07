package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class Intent {
	
	private String intentKeyword;
	private float confidence;
	private IntentType intentType;

	private HashMap<String, Entity> entities;

	private static String[][] UMLAUT_REPLACEMENTS = { { new String("Ä"), "Ae" }, { new String("Ü"), "Ue" },
			{ new String("Ö"), "Oe" }, { new String("ä"), "ae" }, { new String("ü"), "ue" }, { new String("ö"), "oe" },
			{ new String("ß"), "ss" } };

	public static String replaceUmlaute(String orig) {
		String result = orig;
		for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++)
			result = result.replace(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);

		return result;
	}

	/**
	 * Constructor
	 * 
	 * @param keyword    of intent corresponding to NLU module
	 * @param confidence of intent keyword
	 */
	public Intent(String keyword, float confidence) {

		this.intentKeyword = replaceUmlaute(keyword);
		this.confidence = confidence;
		this.intentType = getIntentType();
		this.entities = new HashMap<String, Entity>();
	}

	/**
	 * Constructor for intent extraction through Rasa NLU.
	 * 
	 * @param json representation of Rasa NLU results
	 */
	public Intent(JSONObject json) {
		JSONObject intentInner = (JSONObject) json.get("intent");
		float confidence = intentInner.getAsNumber("confidence").floatValue();
		this.intentKeyword = replaceUmlaute(intentInner.getAsString("name"));
		this.confidence = confidence;

		JSONArray entities = (JSONArray) json.get("entities");
		HashMap<String, Entity> entitiesMap = new HashMap<String, Entity>();
		entities.forEach(o -> {
			Entity entity = new Entity((JSONObject) o);
			entitiesMap.put(entity.getEntityName(), entity);
		});
		this.entities = entitiesMap;
		this.intentType = getIntentType();
	}

	/**
	 * Constructor for bypassing intent extraction. Used for '!'-commands, for
	 * example.
	 * 
	 * @param intentKeyword
	 * @param entityName
	 * @param entityValue
	 */
	public Intent(String intentKeyword, String entityName, String entityValue) {
		this.intentKeyword = replaceUmlaute(intentKeyword);
		this.confidence = 1.0f;
		this.entities = new HashMap<String, Entity>();
		this.entities.put(entityName, new Entity(entityName, entityValue));
		this.intentType = getIntentType();
	}

	public String getKeyword() {
		return this.intentKeyword;
	}

	public void setKeyword(String keyword) {
		this.intentKeyword = keyword;
	}

	public float getConfidence() {
		return this.confidence;
	}

	public Entity getEntity(String entity) {
		return this.entities.get(entity);
	}

	public void addEntity(String key, Entity entity) {
		this.entities.put(key, entity);
	}
	
	public void addEntities(Collection<Entity> entities) {
		for(Entity entity: entities) {
			addEntity(entity.getEntityName(), entity);
		}
	}
	
	public boolean hasEntity() {
		return (this.entities != null && !this.entities.isEmpty());
	}

	public ArrayList<String> getEntitieValues() {
		ArrayList<String> extractedEntitieValues = new ArrayList<String>();
		for (Entry<String, Entity> entry : entities.entrySet()) {
			String value = entry.getValue().getValue();
			extractedEntitieValues.add(value);
		}
		return extractedEntitieValues;
	}

	public Collection<Entity> getEntities() {
		if (this.entities != null)
			return this.entities.values();
		return new ArrayList<Entity>();
	}

	public IntentType deriveType() {

		assert this.intentKeyword != null : "no intent keyword set";

		String intent = this.getKeyword();
		if (intent.startsWith("info"))
			return IntentType.INFORM;
		if (intent.startsWith("requ"))
			return IntentType.REQUEST;
		if (intent.startsWith("conf"))
			return IntentType.CONFIRM;
		if (intent.startsWith("deny"))
			return IntentType.DENY;
		if (intent.startsWith("revert"))
			return IntentType.REVERT;
		if (intent.startsWith("cancel"))
			return IntentType.CANCEL;
		if (intent.contentEquals("start"))
			return IntentType.START;
		if (intent.contentEquals("help"))
			return IntentType.HELP;

		return IntentType.UNKOWN;
	}

	public IntentType getIntentType() {

		return this.intentType;
	}

	public void setIntentType(IntentType intentType) {
		this.intentType = intentType;
	}

	@Override
	public String toString() {
		return "Intent [intentKeyword=" + intentKeyword + ", intentType=" + intentType + ", entities=" + entities + "]";
	}

}
