package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashMap;
import java.util.Map;


public class Selection implements MessengerElement {
	
	/**
	 * Intent to activate this selection
	 */
	String intent;
	
	String message;
	
	boolean operation;
	
	String operationName;
	
	String operationDescription;
	
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
	
	public boolean isOperation() {
		return operation;
	}

	public void setOperation(boolean operation) {
		this.operation = operation;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getOperationDescription() {
		return operationDescription;
	}

	public void setOperationDescription(String operationDescription) {
		this.operationDescription = operationDescription;
	}

	@Override
	public String getIntentKeyword() {		
		return this.intent;
	}
	
	public String getActIntent() {		
		return "selection_" + intent;		
	}
	
}
