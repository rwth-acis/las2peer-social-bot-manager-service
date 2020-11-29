package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ParameterType;

public class ServiceFunctionAttribute {
	private String id;
	private String name;
	private ParameterType parameterType;
	private boolean sameAsTrigger = false;
	private ServiceFunctionAttribute mappedTo;
	private ArrayList<ServiceFunctionAttribute> childAttributes;
	private ServiceFunctionAttribute parent;
	private ContentGenerator generator;
	private ServiceFunction function;
	private IfThenBlock itb;

	private ServiceFunction retrieveFunction;
	private String retrieveFunctionKey;

	private boolean staticContent;
	private String content;
	private String contentURL;
	private String contentURLKey;
	private String contentType;
	// this attribute will dissapear as everything will be done with a single
	// content attribute
	private String nluQuizContent;

	private boolean file;
	// retrieved by swagger
	private String description;
	private String example;
	private List<String> enumList;
	private boolean required;
	private boolean array;
	private String discriminator;
	private String format;

	public ServiceFunctionAttribute() {
		this.childAttributes = new ArrayList<ServiceFunctionAttribute>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdName() {
		if (this.parent != null)
			return this.parent.getIdName() + "_" + this.name;
		return this.name;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String type) {
		this.contentType = type;
	}

	public ArrayList<ServiceFunctionAttribute> getChildAttributes() {
		return childAttributes;
	}

	public ArrayList<ServiceFunctionAttribute> getAllAttributes() {
		ArrayList<ServiceFunctionAttribute> attributes = new ArrayList<>();
		attributes.add(this);
		for (ServiceFunctionAttribute attr : this.childAttributes) {
			attributes.addAll(attr.getAllAttributes());
		}
		return attributes;
	}

	/*
	 * public void setChildAttributes(ArrayList<ServiceFunctionAttribute>
	 * childAttributes) { this.childAttributes = childAttributes; }
	 */

	public void addChildAttribute(ServiceFunctionAttribute childAttribute) {
		if (childAttribute == null) {
			System.out.println("child is null");
			return;
		}
		this.childAttributes.add(childAttribute);
		System.out.println("My child is " + childAttribute.getName());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isSameAsTrigger() {
		return sameAsTrigger;
	}

	public void setSameAsTrigger(boolean sameAsTrigger) {
		this.sameAsTrigger = sameAsTrigger;
	}

	public ContentGenerator getGenerator() {
		return generator;
	}

	public void setGenerator(ContentGenerator generator) {
		this.generator = generator;
	}

	public ServiceFunctionAttribute getMappedTo() {
		return mappedTo;
	}

	public void setMappedTo(ServiceFunctionAttribute mappedTo) {
		this.mappedTo = mappedTo;
	}

	public ServiceFunction getFunction() {
		return function;
	}

	public void setFunction(ServiceFunction function) {
		this.function = function;
	}

	public boolean hasStaticContent() {
		return staticContent;
	}

	public void setStaticContent(boolean staticContent) {
		this.staticContent = staticContent;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentURL() {
		return contentURL;
	}

	public void setContentURL(String contentURL) {
		this.contentURL = contentURL;
	}

	public ParameterType getParameterType() {
		return parameterType;
	}

	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
	}

	public ServiceFunctionAttribute getParent() {
		return parent;
	}

	public void setParent(ServiceFunctionAttribute parent) {
		System.out.println("My parent is " + parent.getName());
		this.parent = parent;
	}

	public IfThenBlock getItb() {
		return itb;
	}

	public void setItb(IfThenBlock itb) {
		this.itb = itb;
	}

	public boolean hasChildren() {
		return (this.childAttributes != null && !this.childAttributes.isEmpty());
	}

	@Override
	public String toString() {
		return "ServiceFunctionAttribute [id=" + id + ", name=" + name + ", parameterType=" + parameterType
				+ ", sameAsTrigger=" + sameAsTrigger + ", mappedTo=" + mappedTo + ", childAttributes=" + childAttributes
				+ ", parent=" + parent + ", generator=" + generator + ", function=" + function + ", itb=" + itb
				+ ", staticContent=" + staticContent + ", content=" + content + ", contentURL=" + contentURL
				+ ", contentType=" + contentType + "]";
	}

	public String toStringNoChildren() {
		return "ServiceFunctionAttribute [id=" + id + ", name=" + name + ", parameterType=" + parameterType
				+ ", generator=" + generator + ", function=" + function + ", itb=" + itb + ", staticContent="
				+ staticContent + ", content=" + content + ", contentURL=" + contentURL + ", contentType=" + contentType
				+ ", number of children=" + this.childAttributes.size() + ", description=" + description + ", example="
				+ example + "]";
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public List<String> getEnumList() {

		if (this.enumList != null)
			return this.enumList;
		return getUpdatedEnumList();

	}

	public void update() {

		System.out.println("update");
		System.out.println("content url: " + this.contentURL);

		if (this.retrieveFunction == null && this.contentURL == null)
			return;

		List<String> retrievedEnums = null;

		if (this.contentURL != null) {
			ServiceFunction action = new ServiceFunction();
			action.setHttpMethod("GET");
			action.setBasePath(contentURL);
			action.setFunctionPath("");
			action.setActionType(ActionType.FUNCTION);
			retrievedEnums = (List<String>) OpenAPIConnector.readEnums(action, contentURLKey);

		} else if (this.retrieveFunction != null) {
			retrievedEnums = (List<String>) OpenAPIConnector.readEnums(this.retrieveFunction, this.retrieveFunctionKey);
		}

		if (retrievedEnums != null && !retrievedEnums.isEmpty())
			this.enumList = retrievedEnums;

	}

	public List<String> getUpdatedEnumList() {

		if (this.enumList != null && !this.enumList.isEmpty())
			return this.enumList;

		update();
		return this.enumList;
	}

	public void setEnumList(List<String> list) {
		this.enumList = list;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isArray() {
		return this.array;
	}

	public void setArray(boolean value) {
		this.array = value;
	}

	public String getDiscriminator() {
		return discriminator;
	}

	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	public boolean isDiscriminator() {
		return this.parameterType == ParameterType.DISCRIMINATOR;
	}

	public boolean hasDiscriminator() {
		for (ServiceFunctionAttribute attr : this.childAttributes) {
			if (attr.isDiscriminator())
				return true;
		}
		return false;
	}

	public ServiceFunctionAttribute getDiscriminatorAttribute() {
		for (ServiceFunctionAttribute attr : this.childAttributes) {
			if (attr.isDiscriminator())
				return attr;
		}
		return null;
	}

	public List<ServiceFunctionAttribute> getChildren(String dis) {
		List<ServiceFunctionAttribute> list = new ArrayList<ServiceFunctionAttribute>();
		for (ServiceFunctionAttribute attr : this.childAttributes) {
			if (attr.getDiscriminator() != null && attr.getDiscriminator().contentEquals(dis))
				list.add(attr);
		}

		return list;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public ServiceFunction getRetrieveFunction() {
		return retrieveFunction;
	}

	public void setRetrieveFunction(ServiceFunction retrieveFunction) {
		this.retrieveFunction = retrieveFunction;
	}

	public void setRetrieveFunctionKey(String key) {
		this.retrieveFunctionKey = key;
	}

	public String getRetrieveFunctionKey() {
		return this.retrieveFunctionKey;
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(boolean file) {
		this.file = file;
	}

	public String getContentURLKey() {
		return contentURLKey;
	}

	public void setContentURLKey(String contentURLKey) {
		this.contentURLKey = contentURLKey;
	}

	public ServiceFunctionAttribute merge(ServiceFunctionAttribute attr) {
		assert attr != null;

		if (attr.getContentURL() != null)
			this.setContentURL(attr.getContentURL());
		if (attr.getContentURLKey() != null)
			this.setContentURLKey(attr.getContentURLKey());
		if (attr.getContent() != null)
			this.setContent(attr.getContent());
		if (attr.getRetrieveFunction() != null)
			this.setRetrieveFunction(attr.getRetrieveFunction());
		if (attr.getRetrieveFunctionKey() != null)
			this.setRetrieveFunctionKey(attr.getRetrieveFunctionKey());
		
		return attr;

	}

}
