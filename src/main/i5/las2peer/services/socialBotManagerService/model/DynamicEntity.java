package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;

public class DynamicEntity extends IntentEntity {
	
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

	public DynamicEntity(String entityName, ServiceFunction function, String key) {
		super(entityName);
		this.enumList = new ArrayList<>();
		this.generatorFunction = function;
		this.generatorKey = key;
		invariant();
	}
	
	public void setKey(String key) {
		this.generatorKey = key;
	}
	
	public void setFunction(ServiceFunction function) {
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

		System.out.println("update dynamic enum: " + super.entityKeyword);
		List<String> retrievedEnums = (List<String>) OpenAPIConnector.readEnums(generatorFunction, generatorKey);

		if (retrievedEnums != null && !retrievedEnums.isEmpty())
			this.enumList = retrievedEnums;

	}
	
	public void invariant() {
		assert this.enumList != null;
		assert this.generatorFunction != null;
		assert this.generatorKey != null;
	}

}
