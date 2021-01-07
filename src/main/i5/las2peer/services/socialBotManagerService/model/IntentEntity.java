package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;

public class IntentEntity {
	
	/**
	 * Keyword to identify this entity
	 */
	String entityKeyword;

	public IntentEntity(String entity) {
		this.entityKeyword = entity;
	}

	public String getEntityKeyword() {
		return this.entityKeyword;
	}
		
	/**
	 * list contains the values of this entity
	 */
	List<String> enumList;

	/**
	 * function to retrieve the values for this entity
	 */
	ServiceFunction generatorFunction;

	/**
	 * key name to specify entity values from JSON response
	 */
	String generatorKey;

	public IntentEntity(String entityName, ServiceFunction function, String key) {
		this(entityName);
		this.enumList = new ArrayList<>();
		this.generatorFunction = function;
		this.generatorKey = key;
		invariant();
	}

	public void setKey(String key) {
		this.generatorKey = key;
	}
	
	public void setFunction(ServiceFunction function) {
		this.enumList = new ArrayList<>();
		this.generatorFunction = function;
	}

	public boolean contains(String value) {
		
		if(this.enumList.isEmpty())
			this.update();
		
		return this.enumList.contains(value);
	}

	public List<String> getValues() {		
		if(this.enumList.isEmpty())
			return getUpdatedValues();
		
		return this.enumList;
	}
	
	public List<String> getUpdatedValues() {
		this.update();
		return this.enumList;
	}

	private void update() {

		System.out.println("update dynamic enum: " + this.entityKeyword);
		List<String> retrievedEnums = (List<String>) OpenAPIConnector.readEnums(generatorFunction, generatorKey);

		if (retrievedEnums != null && !retrievedEnums.isEmpty())
			this.enumList = retrievedEnums;

	}
	
	public void invariant() {
		assert this.entityKeyword != null;		
	}
	
	public void invariantDynamic() {	
		assert this.generatorFunction != null : "generatorFunction is null";
		assert this.enumList != null : "enumList is null";
		assert this.generatorKey != null: "generatorKey is null";
	}
}
