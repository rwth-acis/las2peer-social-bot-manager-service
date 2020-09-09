package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.List;

public class ServiceFunctionAttribute {
	private String id;
	private String name;
	private String parameterType;
	private boolean sameAsTrigger = false;
	private ServiceFunctionAttribute mappedTo;
	private ArrayList<ServiceFunctionAttribute> childAttributes;
	private ServiceFunctionAttribute parent;
	private ContentGenerator generator;
	private ServiceFunction function;
	private IfThenBlock itb;

	private boolean staticContent;
	private String content;
	private String contentURL;
	private String contentType;
    // this attribute will dissapear as everything will be done with a single content attribute
    private String nluQuizContent;
    
    	//retrieved by swagger
    	private String description;
    	private String example;
    	private List<Object> enumList;
    	private boolean required;
    	
	public ServiceFunctionAttribute() {
		this.childAttributes = new ArrayList<ServiceFunctionAttribute>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	/*public void setChildAttributes(ArrayList<ServiceFunctionAttribute> childAttributes) {
		this.childAttributes = childAttributes;
	}*/

	public void addChildAttribute(ServiceFunctionAttribute childAttribute) {
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

	public String getParameterType() {
		return parameterType;
	}

	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public ServiceFunctionAttribute getParent() {
		return parent;
	}

	public void setParent(ServiceFunctionAttribute parent) {
		System.out.println("My parent is " +parent.getName());
		this.parent = parent;
	}

	public IfThenBlock getItb() {
		return itb;
	}

	public void setItb(IfThenBlock itb) {
		this.itb = itb;
	}
    


    

	@Override
	public String toString() {		
		return "ServiceFunctionAttribute [id=" + id + ", name=" + name + ", parameterType=" + parameterType
				+ ", sameAsTrigger=" + sameAsTrigger + ", mappedTo=" + mappedTo + ", childAttributes=" + childAttributes
				+ ", parent=" + parent + ", generator=" + generator + ", function=" + function + ", itb=" + itb
				+ ", staticContent=" + staticContent + ", content=" + content + ", contentURL=" + contentURL
				+ ", contentType=" + contentType + "]";
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

	public List<Object> getEnumList() {
	    return enumList;
	}

	public void setEnumList(List<Object> list) {
	    this.enumList = list;
	}

	public boolean isRequired() {
	    return required;
	}

	public void setRequired(boolean required) {
	    this.required = required;
	}

}
