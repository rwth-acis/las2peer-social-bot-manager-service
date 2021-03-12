package i5.las2peer.services.socialBotManagerService.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import i5.las2peer.services.socialBotManagerService.parser.openapi.FunctionInterface;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ParameterType;

public class ServiceFunction extends TriggerFunction implements FunctionInterface {

	private String id;
	private Set<Bot> bots;
	private Set<VLEUser> users;

	private String name;
	private String method;
	private String path;
	private String consumes;
	private String produces;
	private String description;
	private Set<ServiceFunctionAttribute> attributes;
	private ServiceFunctionAttribute response;

	private Set<Trigger> trigger;

	private Service service;
	private String serviceName;

	private ActionType actionType = ActionType.SERVICE;
	private String messengerName;

	/**
	 * Create empty Service Function
	 */
	public ServiceFunction() {
		this.attributes = new HashSet<ServiceFunctionAttribute>();
		setBots(new HashSet<Bot>());
		setUsers(new HashSet<VLEUser>());
		setTrigger(new HashSet<Trigger>());
	}

	/**
	 * Create a service function, that needs to be completed by a swagger
	 * call
	 * 
	 * @param id
	 * @param service
	 * @param functionName
	 */
	public ServiceFunction(String id, Service service, String functionName) {
		this();
		assert service != null : "function created without service";
		assert functionName != null : "function created without function path";
		this.service = service;
		this.name = functionName;
	}
	
	/**
	 * Create a simple GET function of a specified URL
	 * 
	 * @param fullURL
	 */
	public ServiceFunction(URL fullURL) {
		this(UUID.randomUUID().toString(), new Service(fullURL), "");
		this.method = "GET";
		this.actionType = (ActionType.FUNCTION);
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
		if (this.service != null)
			return this.service.getServiceType();
		return ServiceType.OPENAPI;
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

	@Override
	public String getFunctionName() {
		return name;
	}

	public void setFunctionName(String functionName) {
		this.name = functionName;
	}

	@Override
	public String getFunctionPath() {
		return path;
	}

	public void setFunctionPath(String functionPath) {
		this.path = functionPath;
	}

	@Override
	public String getBasePath() {

		if (this.service == null)
			return this.serviceName;

		return service.getServiceURL();
	}

	public void setBaseURL(URL path) {
		assert path != null;
		this.setBasePath(path.toString());
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

	@Override
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

	@Override
	public boolean contains(ServiceFunctionAttribute attr) {
		for (ServiceFunctionAttribute attr2 : this.getAllAttributes()) {
			if (attr.equals(attr2))
				return true;
		}
		return false;
	}

	@Override
	public ServiceFunctionAttribute getAttribute(String name) {
		List<ServiceFunctionAttribute> attributes = this.getAllAttributes();
		for (ServiceFunctionAttribute attr : attributes) {
			if (attr.getIdName().equalsIgnoreCase(name) || attr.getId().contentEquals(name))
				return attr;
		}

		return null;
	}

	@Override
	public List<ServiceFunctionAttribute> getRequiredAttributes() {
		List<ServiceFunctionAttribute> res = new ArrayList<ServiceFunctionAttribute>();
		for (ServiceFunctionAttribute attr : this.attributes) {
			if (attr.isRequired())
				res.add(attr);
		}
		return res;
	}

	public ServiceFunctionAttribute getBodyAttribute() {
		for (ServiceFunctionAttribute attr : this.attributes) {
			if (attr.getParameterType() == ParameterType.BODY)
				return attr;
		}
		return null;
	}

	@Override
	public List<ServiceFunctionAttribute> getPathAttributes() {
		List<ServiceFunctionAttribute> res = new ArrayList<ServiceFunctionAttribute>();
		for (ServiceFunctionAttribute attr : this.attributes) {
			if (attr.getParameterType() == ParameterType.PATH)
				res.add(attr);
		}
		return res;
	}

	@Override
	public List<ServiceFunctionAttribute> getQueryAttributes() {
		List<ServiceFunctionAttribute> res = new ArrayList<ServiceFunctionAttribute>();
		for (ServiceFunctionAttribute attr : this.attributes) {
			if (attr.getParameterType() == ParameterType.QUERY)
				res.add(attr);
		}
		return res;
	}

	@Override
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
	
	public ServiceFunctionAttribute getResponse() {
		return response;
	}

	public void setResponse(ServiceFunctionAttribute response) {
		this.response = response;
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

	public boolean hasOpenAttribute() {
		for (ServiceFunctionAttribute attr : this.getAllAttributes())
			if (attr.isOpen())
				return true;

		return false;
	}

	public boolean hasFrameGeneratedAttribute() {
		System.out.println("search for frame generated attribute in " + this.getFunctionName());
		for (ServiceFunctionAttribute attr : this.getAllAttributes()) {
			System.out.println("search in attr " + attr.getName() + " " + attr.getSlotID());

			if (attr.getSlotID() != null && !attr.getSlotID().contentEquals("")) {
				System.out.println("found");
				return true;
			}
		}
		return false;
	}

	public Collection<ServiceFunctionAttribute> getFrameGeneratedAttributes() {
		Collection<ServiceFunctionAttribute> res = new ArrayList<>();
		for (ServiceFunctionAttribute attr : this.getAllAttributes()) {
			if (attr.getSlotID() != null && !attr.getSlotID().contentEquals(""))
				res.add(attr);
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

		if (!this.getBasePath().startsWith("http") && function.getBasePath().startsWith("http"))
			this.setBasePath(function.getBasePath());

		if (this.actionType == ActionType.SERVICE && function.getActionType() == ActionType.FUNCTION)
			this.actionType = function.getActionType();

		if(function.getHttpMethod() != null)
			this.method = function.getHttpMethod();
		
		for (ServiceFunctionAttribute attr : this.getAllAttributes()) 
			System.out.println( " " + attr.getIdName());
		

		for (ServiceFunctionAttribute mergeAttr : function.getAttributes()) {
			if (this.getAttribute(mergeAttr.getIdName()) != null) {
				//System.out.println("attribute identified: " + mergeAttr.getIdName());
				ServiceFunctionAttribute myAttr = this.getAttribute(mergeAttr.getIdName());
				myAttr.merge(mergeAttr);
			} else {
				System.out.println("!cant merge attribute: " + mergeAttr.getIdName());
			}
		}

		this.invariant();		
		return this;
	}

	public void invariant() {

		assert this.name != null : "function has no name";
		assert this.service != null || this.serviceName != null : "function " + this.name + " has no service";
		assert this.actionType != null : "function " + this.name + " has no action type";
		assert this.getHttpMethod() != null: "no http method";
		assert this.getBasePath() != null: "no base path";
		
	}

	@Override
	public ServiceFunction asServiceFunction() {
		return this;
	}

	public String prettyPrint() {
		
		String res = "--- ServiceFunction: " + this.getFunctionName();
		for(ServiceFunctionAttribute attr : this.attributes)
			res = "\n" + res + attr.prettyPrint(0);
		
		return res;
	}
}
