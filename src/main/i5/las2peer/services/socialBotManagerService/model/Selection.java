package i5.las2peer.services.socialBotManagerService.model;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.dialogue.Command;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ResponseParseMode;


public class Selection implements MessengerElement, Menuable, DynamicResponse{

	/**
	 * UUID id
	 */
	final String id;
	
	/**
	 * intent to activate this selection
	 */
	String intent;
	
	/**
	 * elements that are selected by intent (key: intent, value: selected element)
	 */
	Map<String, MessengerElement> elements;

	/**
	 *  name of parameter attribute that is filled by this selection 
	 */
	String parameterName;
	
	// Menu Operation	
	boolean operation;		
	String operationName;	
	String operationDescription;
	
	// Response Message
	ResponseParseMode parseMode;
	ServiceFunction responseFunction;
	String responseMessage;
	URL responseURL;
			
	// Generated Selection
	DynamicEntity dynamicEntity;
	ServiceFunction generatorFunction;
	String generatorKey;
	URL generatorURL;	
		
	public Selection(String key) {
		this.id = key;
		elements = new HashMap<>();
	}
	
	public boolean isDynamic() {
		return this.dynamicEntity != null;
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
	
	public Collection<String> getEnums() {
		
		if(!this.isDynamic())
			return this.elements.keySet();
		
		return this.dynamicEntity.getValues();
	}
	
	@Override
	public Command getCommand() {
		
		if(!this.isOperation())
			return null;
		
		Command res = new Command();
		res.setIntent(this.getIntentKeyword());
		res.setName(this.operationName);
		res.setDescription(this.operationDescription);
		res.invariant();
		return res;

	}
	
	public String getMessage() {
		return this.responseMessage;
	}

	public void setMessage(String message) {
		this.responseMessage = message;
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
	
	public void setParameterName(String name) {
		this.parameterName = name;
	}
	
	public String getParameterName() {
		return this.parameterName;
	}
	
	public DynamicEntity getDynamicEntity() {
		return dynamicEntity;
	}

	public void setDynamicEntity(DynamicEntity dynamicEntity) {
		this.dynamicEntity = dynamicEntity;
	}
	
	public URL getResponseURL() {
		return responseURL;
	}

	public void setResponseURL(URL responseURL) {
		this.responseURL = responseURL;
	}

	@Override
	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	public ResponseParseMode getParseMode() {
		return parseMode;
	}

	public void setParseMode(ResponseParseMode parseMode) {
		this.parseMode = parseMode;
	}

	public URL getGeneratorURL() {
		return generatorURL;
	}

	public void setGeneratorURL(URL generatorURL) {
		this.generatorURL = generatorURL;
	}

	public String getGeneratorKey() {
		return generatorKey;
	}
	
	public void setGeneratorFunction(ServiceFunction function) {
		this.generatorFunction = function;
	}

	public void setGeneratorKey(String generatorKey) {
		this.generatorKey = generatorKey;
	}
		
	public boolean fillsParameter() {
		return this.parameterName != null && !this.parameterName.contentEquals("");
	}
	
	@Override
	public String getIntentKeyword() {		
		return this.intent;
	}

	@Override
	public String getActIntent() {		
		return "selection_" + intent;		
	}

	@Override
	public ResponseParseMode getResponseParseMode() {		
		return this.parseMode;
	}

	@Override
	public ServiceFunction getResponseFunction() {
		return this.responseFunction;
	}
		
}
