package i5.las2peer.services.socialBotManagerService.parser.training;

import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

public class BotMonitorEventBodyGenerator {

    String serviceAlias;

    public BotMonitorEventBodyGenerator(String serviceAlias) {
	this.serviceAlias = serviceAlias;
    }

    public String eventBody(String eventId, String functionName, Map<String, String> attributes) {

	JSONObject object = new JSONObject();
	object.put("eventId", eventId);
	object.put("serviceAlias", serviceAlias);
	object.put("functionName", functionName.toLowerCase());
	object.put("eventName", functionName.toLowerCase());

	if (attributes != null) {
	    JSONObject attObject = new JSONObject();

	    for (Entry<String, String> attribute : attributes.entrySet()) {
		attObject.put(attribute.getKey(), attribute.getValue());
	    }
	    object.put("attributes", attObject);
	}

	return object.toString();
    }

}
