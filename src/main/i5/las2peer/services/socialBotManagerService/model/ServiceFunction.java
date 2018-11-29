package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.HashSet;

public class ServiceFunction {
	private String id;
	private String functionName;
	private String serviceName;
	private ArrayList<ServiceFunctionAttribute> attributes;
	private HashSet<String> bots;
	private String path;
	private String type;
	private String consumes;
	private String produces;
	private String functionType = "service";
	private String conversationType;
	private String token;
	private ServiceFunction tf;

	public ServiceFunction() {
		attributes = new ArrayList<ServiceFunctionAttribute>();
		bots = new HashSet<String>();
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String name) {
		this.functionName = name;
	}

	public ArrayList<ServiceFunctionAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<ServiceFunctionAttribute> attributes) {
		this.attributes = attributes;
	}

	public void addAttribute(ServiceFunctionAttribute attribute) {
		this.attributes.add(attribute);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getConsumes() {
		return consumes;
	}

	public void setConsumes(String consumes) {
		this.consumes = consumes;
	}

	public String getProduces() {
		return produces;
	}

	public void setProduces(String produces) {
		this.produces = produces;
	}

	public String getFunctionType() {
		return functionType;
	}

	public void setFunctionType(String functionType) {
		this.functionType = functionType;
	}

	public String getConversationType() {
		return conversationType;
	}

	public void setConversationType(String conversationType) {
		this.conversationType = conversationType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public HashSet<String> getBots() {
		return bots;
	}

	public void setBots(HashSet<String> bots) {
		this.bots = bots;
	}

	public void addBot(String name) {
		this.bots.add(name);
	}

	public ServiceFunction getTf() {
		return tf;
	}

	public void setTf(ServiceFunction tf) {
		this.tf = tf;
	}
}
