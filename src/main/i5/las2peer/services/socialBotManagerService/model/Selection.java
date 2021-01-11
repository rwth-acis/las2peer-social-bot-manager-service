package i5.las2peer.services.socialBotManagerService.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.dialogue.Command;

public class Selection implements MessengerElement, Menuable, DynamicResponse {

	/**
	 * UUID id
	 */
	final String id;

	/**
	 * intent to activate this selection
	 */
	String intent;

	/**
	 * Response Message
	 */
	String response;
	
	/**
	 * elements that are selected by intent (key: intent, value: selected element)
	 */
	Map<String, MessengerElement> elements;

	/**
	 * name of parameter attribute that is filled by this selection
	 */
	String parameterName;

	// Menu Operation
	Command command;
	
	// Dynamic Selection Entities
	IntentEntity dynamicEntity;
	
	// Generated Response Message
	GeneratorFunction generatorFunction;

	public Selection(String key, String intent, String response) {
		assert key != null;
		
		this.id = key;
		this.intent = intent;
		this.response = response;
		elements = new HashMap<>();
	}

	public boolean isDynamic() {
		return this.dynamicEntity != null;
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

		if (!this.isDynamic())
			return this.elements.keySet();

		return this.dynamicEntity.getValues();
	}

	@Override
	public boolean isOperation() {
		return this.getCommand() != null;
	}
	
	@Override
	public Command getCommand() {
		return this.command;
	}
	
	public void setCommand(Command command) {
		this.command = command;
	}
	
	public void setParameterName(String name) {
		this.parameterName = name;
	}

	public String getParameterName() {
		return this.parameterName;
	}

	public IntentEntity getDynamicEntity() {
		return dynamicEntity;
	}

	public void setDynamicEntity(IntentEntity entity) {
		this.dynamicEntity = entity;
	}
	
	public void setGeneratorFunction(GeneratorFunction function) {
		this.generatorFunction = function;
	}

	public boolean fillsParameter() {
		return this.parameterName != null && !this.parameterName.contentEquals("");
	}

	@Override
	public GeneratorFunction getGeneratorFunction() {
		return this.generatorFunction;
	}
	
	@Override
	public String getIntentKeyword() {
		return this.intent;
	}
	
	@Override
	public String getActIntent() {
		return this.intent + "_response";
	}

	public String getResponseMessage() {
		return this.response;
	}

}
