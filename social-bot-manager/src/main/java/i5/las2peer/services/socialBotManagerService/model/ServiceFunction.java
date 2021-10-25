package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashSet;

public class ServiceFunction extends TriggerFunction{
	private String id;
	private String serviceName;
	private HashSet<Bot> bots;
	private HashSet<VLEUser> users;
	private String functionName;
	private String functionPath;
	private String httpMethod;
	private String consumes;
	private String produces;
	private ActionType actionType = ActionType.SERVICE;
	private String messengerName;
	private HashSet<ServiceFunctionAttribute> attributes;
	private HashSet<Trigger> trigger;

	public ServiceFunction() {
		setAttributes(new HashSet<ServiceFunctionAttribute>());
		setBots(new HashSet<Bot>());
		setUsers(new HashSet<VLEUser>());
		setTrigger(new HashSet<Trigger>());
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

	public HashSet<Bot> getBots() {
		return bots;
	}

	public void setBots(HashSet<Bot> bots) {
		this.bots = bots;
	}

	public void addBot(Bot b) {
		this.bots.add(b);
	}

	public HashSet<VLEUser> getUsers() {
		return users;
	}

	public void setUsers(HashSet<VLEUser> users) {
		this.users = users;
	}

	public void addUser(VLEUser u) {
		this.users.add(u);
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public String getFunctionPath() {
		return functionPath;
	}

	public void setFunctionPath(String functionPath) {
		for (ServiceFunctionAttribute attr : attributes) {
			if (attr.getParameterType().equals("path")) {
				try {
					functionPath = functionPath.replaceAll("\\{" + attr.getName() + "\\}", attr.getContent());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(functionPath);
		this.functionPath = functionPath;
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

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public String getMessengerName() {
		return messengerName;
	}

	public void setMessengerName(String messengerName) {
		this.messengerName = messengerName;
	}

	public HashSet<ServiceFunctionAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(HashSet<ServiceFunctionAttribute> attributes) {
		this.attributes = attributes;
	}

	public void addAttribute(ServiceFunctionAttribute attribute) {
		this.attributes.add(attribute);
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public HashSet<Trigger> getTrigger() {
		return trigger;
	}

	public void setTrigger(HashSet<Trigger> trigger) {
		this.trigger = trigger;
	}

	public void addTrigger(Trigger t) {
		this.trigger.add(t);
	}

}
