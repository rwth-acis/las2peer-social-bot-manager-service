package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class Intent {
	private String intentKeyword;
	private float confidence;

	private HashMap<String, Entity> entities;

	private static String[][] UMLAUT_REPLACEMENTS = { { new String("Ä"), "Ae" }, { new String("Ü"), "Ue" },
			{ new String("Ö"), "Oe" }, { new String("ä"), "ae" }, { new String("ü"), "ue" },
			{ new String("ö"), "oe" }, { new String("ß"), "ss" } };

	public static String replaceUmlaute(String orig) {
		String result = orig;

		for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
			result = result.replace(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
		}

		return result;
	}

	// Constructor for intent extraction through Rasa NLU.
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
	}

	// Constructor for bypassing intent extraction. Used for '!'-commands, for
	// example.
	public Intent(String intentKeyword, String entityName, String entityValue) {
		this.intentKeyword = replaceUmlaute(intentKeyword);
		this.confidence = 1.0f;
		this.entities = new HashMap<String, Entity>();
		this.entities.put(entityName, new Entity(entityName, entityValue));
	}

	public Intent(String keyword, float confidence) {
		this.intentKeyword = replaceUmlaute(keyword);
		this.confidence = confidence;
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

    public ArrayList<String> getEntitieValues(){
        ArrayList<String> extractedEntitieValues= new ArrayList<String>();
        for(Entry<String, Entity> entry : entities.entrySet()) {
            String value = entry.getValue().getValue();
            extractedEntitieValues.add(value);
        }
        return extractedEntitieValues;
    }

	public Collection<Entity> getEntities() {
		return this.entities.values();
	}

}
