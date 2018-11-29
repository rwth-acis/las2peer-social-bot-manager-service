package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashMap;
import java.util.HashSet;

public class Bot {
	private String name;
	private String id;
	private boolean active;
	private String service;
	private String vle;

	private HashMap<String, ServiceFunction> botServiceFunctions;
	private HashMap<String, ServiceFunction> userServiceFunctions;
	private HashMap<String, HashSet<String>> triggerList;
	private HashMap<String, ContentGenerator> generatorList;
	private HashMap<String, ServiceFunctionAttribute> serviceFunctionsAttributes;
	private HashMap<String, IfBlock> attributeIfs;
	private HashMap<String, ThenBlock> attributeThens;
	private HashMap<String, IfThenBlock> attributeIfThens;

	public Bot() {
		botServiceFunctions = new HashMap<String, ServiceFunction>();
		userServiceFunctions = new HashMap<String, ServiceFunction>();
		triggerList = new HashMap<String, HashSet<String>>();
		serviceFunctionsAttributes = new HashMap<String, ServiceFunctionAttribute>();
		generatorList = new HashMap<String, ContentGenerator>();
		attributeIfs = new HashMap<String, IfBlock>();
		attributeThens = new HashMap<String, ThenBlock>();
		attributeIfThens = new HashMap<String, IfThenBlock>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public HashMap<String, ServiceFunction> getBotServiceFunctions() {
		return botServiceFunctions;
	}

	public void setServiceFunctions(HashMap<String, ServiceFunction> serviceFunctions) {
		this.botServiceFunctions = serviceFunctions;
	}

	public void addBotServiceFunction(String name, ServiceFunction serviceFunction) {
		this.botServiceFunctions.put(name, serviceFunction);
	}

	public HashMap<String, ServiceFunctionAttribute> getServiceFunctionsAttributes() {
		return serviceFunctionsAttributes;
	}

	public void setServiceFunctionsAttributes(HashMap<String, ServiceFunctionAttribute> serviceFunctionsAttributes) {
		this.serviceFunctionsAttributes = serviceFunctionsAttributes;
	}

	public void addServiceFunctionsAttributes(String key, ServiceFunctionAttribute serviceFunctionsAttribute) {
		this.serviceFunctionsAttributes.put(key, serviceFunctionsAttribute);
	}

	public HashMap<String, ServiceFunction> getUserServiceFunctions() {
		return userServiceFunctions;
	}

	public void setUserServiceFunctions(HashMap<String, ServiceFunction> userServiceFunctions) {
		this.userServiceFunctions = userServiceFunctions;
	}

	public void addUserServiceFunction(String name, ServiceFunction serviceFunction) {
		this.userServiceFunctions.put(name, serviceFunction);
	}

	public HashMap<String, HashSet<String>> getTriggerList() {
		return triggerList;
	}

	public void setTriggerList(HashMap<String, HashSet<String>> triggerList) {
		this.triggerList = triggerList;
	}

	public void addTrigger(String t, String f) {
		HashSet<String> l = this.triggerList.get(t);
		if (l == null) {
			l = new HashSet<String>();
			l.add(f);
			this.triggerList.put(t, l);
		} else {
			l.add(f);
		}
	}

	public HashMap<String, ContentGenerator> getGeneratorList() {
		return generatorList;
	}

	public void setGeneratorList(HashMap<String, ContentGenerator> generatorList) {
		this.generatorList = generatorList;
	}

	public void addGenerator(String s, ContentGenerator g) {
		this.generatorList.put(s, g);
	}

	public HashMap<String, IfBlock> getAttributeIfs() {
		return attributeIfs;
	}

	public void setAttributeIfs(HashMap<String, IfBlock> attributeIfs) {
		this.attributeIfs = attributeIfs;
	}

	public void addAttributeIf(String key, IfBlock ib) {
		this.attributeIfs.put(key, ib);
	}

	public HashMap<String, ThenBlock> getAttributeThens() {
		return attributeThens;
	}

	public void setAttributeThens(HashMap<String, ThenBlock> attributeThens) {
		this.attributeThens = attributeThens;
	}

	public void addAttributeThen(String key, ThenBlock tb) {
		this.attributeThens.put(key, tb);
	}

	public HashMap<String, IfThenBlock> getAttributeIfThens() {
		return attributeIfThens;
	}

	public void setAttributeIfThens(HashMap<String, IfThenBlock> attributeIfThens) {
		this.attributeIfThens = attributeIfThens;
	}

	public void addAttributeIfThen(String s, IfThenBlock itb) {
		this.attributeIfThens.put(s, itb);
	}

	public String getVle() {
		return vle;
	}

	public void setVle(String vle) {
		this.vle = vle;
	}
}
