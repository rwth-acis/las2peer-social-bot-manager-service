package i5.las2peer.services.socialBotManagerService.dialogue.notification;

import java.util.Map;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class EventMessage {

	@Override
    public String toString() {
	return "EventMessage [eventId=" + eventId + ", eventName=" + eventName + ", serviceAlias=" + serviceAlias
		+ ", functionName=" + functionName + ", attributes=" + attributes + "]";
    }

	String eventId;
	String eventName;

	String serviceAlias;
	String functionName;
	String uid;

	Map<String, String> attributes;

	public EventMessage(String jsonBody) {
		this.parse(jsonBody);
	}

	public void parse(String jsonBody) {
		
		try {

			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject parsedBody = (JSONObject) parser.parse(jsonBody);
			
			if(parsedBody.containsKey("eventId"))
				this.eventId = parsedBody.getAsString("eventId");
			if(parsedBody.containsKey("eventName"))
				this.eventName = parsedBody.getAsString("eventName");
			
			if(parsedBody.containsKey("serviceAlias"))
				this.serviceAlias = parsedBody.getAsString("serviceAlias");			
			if(parsedBody.containsKey("functionName"))
				this.functionName = parsedBody.getAsString("functionName");
			if(parsedBody.containsKey("uid"))
				this.uid = parsedBody.getAsString("uid");
								
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getServiceAlias() {
		return serviceAlias;
	}

	public void setServiceAlias(String serviceAlias) {
		this.serviceAlias = serviceAlias;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	public boolean invariant() {
		if(this.functionName == null && this.eventName == null)
			return false;
		return true;
	}

}
