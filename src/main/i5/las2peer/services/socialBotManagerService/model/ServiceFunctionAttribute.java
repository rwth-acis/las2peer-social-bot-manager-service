package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Fillable;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Slotable;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ParameterType;

public class ServiceFunctionAttribute {
	
	private final String id;
	private final String name;
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
	private String contentFill;
	// this attribute will dissapear as everything will be done with a single
	// content attribute
	private String nluQuizContent;
	private boolean file;
	private String entityKeyword;
	private String slotName;

	// retrieved by swagger
	private String description;
	private String example;
	private List<String> enumList;
	private boolean required;
	private boolean array;
	private String discriminator;
	private String format;

	public ServiceFunctionAttribute(String id, String name) {
		this.childAttributes = new ArrayList<ServiceFunctionAttribute>();
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
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
		if (childAttribute == null)
			return;
		this.childAttributes.add(childAttribute);
	}

	public String getId() {
		return id;
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

	public String toStringWithChildren() {
		return "ServiceFunctionAttribute [id=" + id + ", name=" + name + ", parameterType=" + parameterType
				+ ", contentType=" + contentType + ", childAttributes=" + childAttributes + "]";
	}

	public String toStringNoChildren() {
		return "ServiceFunctionAttribute [id=" + id + ", name=" + name + ", parameterType=" + parameterType
				+ ", generator=" + generator + ", function=" + function + ", itb=" + itb + ", staticContent="
				+ staticContent + ", content=" + content + ", contentURL=" + contentURL + ", contentType=" + contentType
				+ ", number of children=" + childAttributes.size() + ", description=" + description + ", example="
				+ example + " slotName=" + slotName +"]";
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

		if (this.retrieveFunction == null || this.retrieveFunction.hasFrameGeneratedAttribute())
			return;

		if (this.contentURL == null)
			return;

		System.out.println("update " + this.getName());
		List<String> retrievedEnums = null;

		if (this.contentURL != null && this.contentURL.startsWith("http")) {
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

	public void update(DialogueGoal goal) {

		assert this.retrieveFunction != null;
		assert this.retrieveFunction.hasFrameGeneratedAttribute();
		System.out.println("update by frame " + goal.getFrame().getName());

		Collection<ServiceFunctionAttribute> attrs = this.retrieveFunction.getFrameGeneratedAttributes();
		OpenAPIAction action = new OpenAPIAction();
		action.setFunction(retrieveFunction);
		for (ServiceFunctionAttribute attr : attrs) {
			if (attr.isFrameGenerated()) {
				if (attr.getParameterType() == ParameterType.PATH) {
					Slotable node = goal.getNode(attr.getSlotName());
					if (node == null) {
						System.out.println("no node named " + attr.getSlotName() + " found");
					} else {
						if (node instanceof Fillable) {
							Fillable fill = (Fillable) node;
							System.out.println("fill " + attr.getName() + " with " + fill.getValue());
							action.addPathParameter(attr.getName(), fill.getValue());
						}
					}
				}

			}
		}
		
		List<String> retrievedEnums = (List<String>) OpenAPIConnector.readEnums(action, this.retrieveFunctionKey);
		System.out.println("enums: " + retrievedEnums);
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

	public void setEntity(IntentEntity entity) {
		this.entityKeyword = entity.getEntityKeyword();
	}

	public String getEntity() {
		return this.entityKeyword;
	}

	public String getContentURLKey() {
		return contentURLKey;
	}

	public void setContentURLKey(String contentURLKey) {
		this.contentURLKey = contentURLKey;
	}

	public boolean hasDynamicEnums() {
		if (this.contentURL != null && !this.contentURL.contentEquals(""))
			return true;
		if (this.retrieveFunction != null)
			return true;
		return false;
	}

	public ServiceFunctionAttribute merge(ServiceFunctionAttribute attr) {
		assert attr != null;

		if (attr.getContentURL() != null)
			this.setContentURL(attr.getContentURL());
		if (attr.getContentURLKey() != null)
			this.setContentURLKey(attr.getContentURLKey());
		if (attr.getContent() != null)
			this.setContent(attr.getContent());
		if (attr.getContentFill() != null)
			this.setContentFill(attr.getContentFill());
		if (attr.getRetrieveFunction() != null)
			this.setRetrieveFunction(attr.getRetrieveFunction());
		if (attr.getRetrieveFunctionKey() != null)
			this.setRetrieveFunctionKey(attr.getRetrieveFunctionKey());

		if (attr.getContentType() != null)
			this.setContentType(attr.getContentType());
		if (attr.getEntity() != null)
			this.setEntity(new IntentEntity(attr.getEntity()));
		if (attr.getSlotName() != null)
			this.setSlotName(attr.getSlotName());

		if (attr.hasDynamicEnums())
			this.setContentType("enum");

		return attr;

	}

	public boolean hasContent() {
		return this.content != null && !this.content.contentEquals("");
	}

	public String getSlotName() {
		return this.slotName;
	}

	public void setSlotName(String slotName) {
		System.out.println("SET SLOT NAME " + slotName);		
		if (slotName != null)
			this.slotName = slotName;
	}

	public String getContentFill() {
		return contentFill;
	}

	public void setContentFill(String contentFill) {
		this.contentFill = contentFill;
	}

	public boolean isFrameGenerated() {
		if (this.slotName != null && !this.slotName.contentEquals(""))
			return true;
		return false;
	}

}
