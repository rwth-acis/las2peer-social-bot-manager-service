package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import i5.las2peer.services.socialBotManagerService.parser.openapi.ParameterType;

public class ServiceFunction extends TriggerFunction {

	private String id;
	private Service service;

	private Set<Bot> bots;
	private Set<VLEUser> users;

	private String name;
	private String method;
	private String path;
	private String consumes;
	private String produces;
	private String description;
	private Set<ServiceFunctionAttribute> attributes;
	private Set<Trigger> trigger;
	private String serviceName;

	private ActionType actionType = ActionType.SERVICE;
	private String messengerName;

	public ServiceFunction() {
		this.attributes = new HashSet<ServiceFunctionAttribute>();
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
		if (this.service != null)
			return this.service.getServiceAlias();
		return this.serviceName;
	}

	public ServiceType getServiceType() {
		return this.service.getServiceType();
	}

	public Set<Bot> getBots() {
		return bots;
	}

	public void setBots(HashSet<Bot> bots) {
		this.bots = bots;
	}

	public void addBot(Bot b) {
		this.bots.add(b);
	}

	public Set<VLEUser> getUsers() {
		return users;
	}

	public void setUsers(HashSet<VLEUser> users) {
		this.users = users;
	}

	public void addUser(VLEUser u) {
		this.users.add(u);
	}

	public String getFunctionName() {
		return name;
	}

	public void setFunctionName(String functionName) {
		this.name = functionName;
	}

	public String getFunctionPath() {
		return path;
	}

	public void setFunctionPath(String functionPath) {
		this.path = functionPath;
	}

	public String getBasePath() {

		System.out.println("service is " + this.service);
		System.out.println("serviceName is " + this.serviceName);

		if (this.service == null)
			return this.serviceName;

		return service.getServiceURL();
	}

	public void setBasePath(String path) {
		this.serviceName = path;
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

	public Set<ServiceFunctionAttribute> getAttributes() {
		return attributes;
	}

	public void addAttribute(ServiceFunctionAttribute attribute) {
		this.attributes.add(attribute);
	}

	public String getHttpMethod() {
		return method;
	}

	public void setHttpMethod(String httpMethod) {
		this.method = httpMethod;
	}

	public Set<Trigger> getTrigger() {
		return trigger;
	}

	public void setTrigger(HashSet<Trigger> trigger) {
		this.trigger = trigger;
	}

	public void addTrigger(Trigger t) {
		this.trigger.add(t);
	}

	public String getSwaggerUrl() {
		if (this.service == null || this.service.getSwaggerURL() == null)
			return null;
		return this.service.getSwaggerURL();
	}

	@Override
	public String toString() {
		return "ServiceFunction [id=" + id + "functionName=" + name + ", functionPath=" + path + ", httpMethod="
				+ method + ", consumes=" + consumes + ", produces=" + produces + " service=" + service + "]";
	}

	public String toStringWithAttributes() {
		String res = toString();
		for (ServiceFunctionAttribute attr : this.attributes) {
			res = res + attr.toStringWithChildren();
		}
		return res;
	}

	public boolean hasAttributes() {
		return (this.attributes != null && !this.attributes.isEmpty());
	}

	public ServiceFunctionAttribute getAttribute(String name) {
		List<ServiceFunctionAttribute> attributes = this.getAllAttributes();
		for (ServiceFunctionAttribute attr : attributes) {
			if (attr.getIdName().equalsIgnoreCase(name))
				return attr;
		}

		return null;
	}

	public ServiceFunctionAttribute getBodyAttribute() {
		for (ServiceFunctionAttribute attr : this.attributes) {
			if (attr.getParameterType() == ParameterType.BODY)
				return attr;
		}
		return null;
	}

	public List<ServiceFunctionAttribute> getPathAttributes() {
		List<ServiceFunctionAttribute> res = new ArrayList<ServiceFunctionAttribute>();
		for (ServiceFunctionAttribute attr : this.attributes) {
			if (attr.getParameterType() == ParameterType.PATH)
				res.add(attr);
		}
		return res;
	}

	public List<ServiceFunctionAttribute> getQueryAttributes() {
		List<ServiceFunctionAttribute> res = new ArrayList<ServiceFunctionAttribute>();
		for (ServiceFunctionAttribute attr : this.attributes) {
			if (attr.getParameterType() == ParameterType.QUERY)
				res.add(attr);
		}
		return res;
	}

	public ArrayList<ServiceFunctionAttribute> getAllAttributes() {
		ArrayList<ServiceFunctionAttribute> attributes = new ArrayList<>();
		for (ServiceFunctionAttribute attr : this.attributes) {
			attributes.addAll(attr.getAllAttributes());
		}
		return attributes;
	}

	public String getFunctionDescription() {
		return description;
	}

	public void setFunctionDescription(String functionDescription) {
		this.description = functionDescription;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	/**
	 * 
	 * @param serviceName
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMessengerName() {
		return messengerName;
	}

	public void setMessengerName(String messengerName) {
		this.messengerName = messengerName;
	}

	public ActionType getActionType() {
		return this.actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public boolean hasFrameGeneratedAttribute() {
		System.out.println("search for frame generated attribute in " + this.getFunctionName());
		for(ServiceFunctionAttribute attr :this.getAllAttributes()) {
			System.out.println("search in attr " + attr.getName() + " " + attr.getSlotName());

			if (attr.getSlotName() != null && !attr.getSlotName().contentEquals("")) {
				System.out.println("found");
				return true;
			}
		}
		return false;
	}
	
	public Collection<ServiceFunctionAttribute> getFrameGeneratedAttributes() {		
		Collection<ServiceFunctionAttribute> res = new ArrayList<>();
		for(ServiceFunctionAttribute attr :this.getAllAttributes()) {
			if (attr.getSlotName() != null && !attr.getSlotName().contentEquals("")) {
				res.add(attr);
			}
		}
		return res;
	}

	/**
	 * Merges two service functions. (e.g a swagger generated function with a
	 * modeled function)
	 * 
	 * @param function with higher priority
	 * @return merged function
	 */
	public ServiceFunction merge(ServiceFunction function) {

		if (this.serviceName == null && function.serviceName != null)
			this.serviceName = function.serviceName;

		for (ServiceFunctionAttribute attr : this.getAllAttributes()) {
			System.out.println(attr.getIdName());
		}

		for (ServiceFunctionAttribute mergeAttr : function.getAttributes()) {
			if (this.getAttribute(mergeAttr.getIdName()) != null) {
				System.out.println("attribute identified: " + mergeAttr.getIdName());
				ServiceFunctionAttribute myAttr = this.getAttribute(mergeAttr.getIdName());
				myAttr.merge(mergeAttr);
			} else {
				System.out.println("cant merge attribute: " + mergeAttr.getIdName());
			}
		}
		return this;
	}

}
