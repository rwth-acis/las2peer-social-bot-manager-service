package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashMap;
import java.util.HashSet;

public class Bot {
	private String name;
	private String id;
	private String version = "1.0.0";
	private String service;
	private VLE vle;
	private HashMap<String,Boolean> active;

	private HashMap<String, ServiceFunction> botServiceFunctions;
	private HashSet<Trigger> triggerList;
	private HashMap<String, ContentGenerator> generatorList;

	public Bot() {
		botServiceFunctions = new HashMap<String, ServiceFunction>();
		triggerList = new HashSet<Trigger>();
		generatorList = new HashMap<String, ContentGenerator>();
		active = new HashMap<String, Boolean>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public HashSet<Trigger> getTriggerList() {
		return triggerList;
	}

	public void setTriggerList(HashSet<Trigger> triggerList) {
		this.triggerList = triggerList;
	}

	public void addTrigger(Trigger t) {
		this.triggerList.add(t);

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

	public VLE getVle() {
		return vle;
	}

	public void setVle(VLE vle) {
		this.vle = vle;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public HashMap<String,Boolean> getActive() {
		return active;
	}

	public void setActive(HashMap<String,Boolean> active) {
		this.active = active;
	}
	
	public void setIdActive(String id, boolean active) {
		this.active.put(id, active);
	}
	
	public void deactivateAll() {
		for (String k : this.active.keySet()) {
			this.active.put(k, false);
		}
	}
	
	public int countActive() {
		int trueCount = 0;
		for(boolean b:active.values()) {
			if(b)
				trueCount++;
		}
		return trueCount;
	}
}
