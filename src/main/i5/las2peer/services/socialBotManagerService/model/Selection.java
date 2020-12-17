package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashMap;
import java.util.Map;


public class Selection implements MessengerElement {
	
	/**
	 * Intent to activate this selection
	 */
	String intent;
	
	String message;
	
	/**
	 * elements that are selected be intent (key: intent, value:selection)
	 */
	Map<String, MessengerElement> elements;
	
	public Selection() {
		elements = new HashMap<>();
	}
	
	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}
	
	public void addElement(String key, MessengerElement element) {
		System.out.println("selection add element " + key + " " + element);
		this.elements.put(key, element);
	}
	
	public void addElement(String key, Frame frame) {
		System.out.println("selection add element " + key + " " + frame);
		this.elements.put(key, frame);
	}
	
	public void addElement(String key, IncomingMessage element) {
		System.out.println("selection add element " + key + " " + element);
		this.elements.put(key, element);
	}
	
	public Map<String, MessengerElement> getElements() {
		return this.elements;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String getIntentKeyword() {		
		return this.intent;
	}
	
	public String getActIntent() {		
		return "selection_" + intent;		
	}
	
}
