package i5.las2peer.services.socialBotManagerService.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Fillable;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Slotable;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.MessageFile;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ParameterInput;
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

	private boolean staticContent;
	private String content;
	private URL contentURL;
	private String contentURLKey;
	private String contentType;
	private String contentFill;
	// this attribute will dissapear as everything will be done with a single
	// content attribute
	private String nluQuizContent;

	private String entityKeyword;
	private MessageFile file;

	// Filling by other elements
	private ServiceFunction fillingFunction;
	private String fillingFunctionKey;
	private String slotID;
	private String slotName;

	// retrieved by swagger
	private String description;
	private String example;
	private List<String> enumList;
	private boolean required;
	private boolean array;
	private int minItems;
	private String discriminator;
	private String format;
	private ParameterInput input;

	public ServiceFunctionAttribute(String name) {
		this(UUID.randomUUID().toString(), name);
	}

	public ServiceFunctionAttribute(String id, String name) {
		assert id != null;
		assert name != null;

		this.childAttributes = new ArrayList<ServiceFunctionAttribute>();
		this.name = name;
		this.id = id;
	}

	public ServiceFunctionAttribute(String id, String name, ParameterType type) {
		this(id, name);
		assert type != null;

		this.parameterType = type;
		if (type == ParameterType.PATH)
			this.required = true;
	}

	/**
	 * UUID
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * name of parameter corresponding to open API
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * identification of parameter with parent attributes (example:
	 * grandparent_parent_parameter).
	 * 
	 * @return
	 */
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

	public URL getContentURL() {
		return this.contentURL;
	}

	public void setContentURL(URL contentURL) {
		this.contentURL = contentURL;
	}

	public ParameterType getParameterType() {
		return parameterType;
	}

	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
		if (parameterType == ParameterType.PATH)
			this.required = true;
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
				+ example + " slotName=" + slotID + "]";
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

		return getUpdatedEnumList();
	}

	public List<String> update() {

		System.out.println("update: " + this.getName() + ", function:" + this.fillingFunctionKey + " "
				+ this.fillingFunction + " , (content: " + this.getContent() + ")");
		if (this.getRetrieveFunction() == null)
			return null;

		if (this.getRetrieveFunction().hasOpenAttribute()) {
			System.out.println("retrieve function has open attributes");
			return null;

		}

		List<String> retrievedEnums = null;

		if (this.contentURL != null) {
			System.out.println("update by contentURL");

			ServiceFunction action = new ServiceFunction(contentURL);
			retrievedEnums = (List<String>) OpenAPIConnector.readEnums(action, contentURLKey);

		} else if (this.fillingFunction != null) {
			System.out.println("update by filling function");
			retrievedEnums = (List<String>) OpenAPIConnector.readEnums(this.fillingFunction, this.fillingFunctionKey);
		}

		if (retrievedEnums != null && !retrievedEnums.isEmpty())
			this.enumList = retrievedEnums;

		return retrievedEnums;

	}

	/**
	 * Get the dynamic enum list for this attribute
	 * 
	 * @param parameters that needs to be filled call the function that receives
	 *                   this enum list.
	 * @return list of enums
	 */
	public List<String> getUpdatedEnumList(Map<String, String> parameters) {

		if(this.fillingFunction == null)
			return this.enumList;
		
		if (parameters == null)
			parameters = new HashMap<>();

		System.out.println("get updated enum list for " + this.getIdName() + " , parameters: " + parameters.size());

		Collection<ServiceFunctionAttribute> attrs = this.fillingFunction.getFrameGeneratedAttributes();
		for (ServiceFunctionAttribute attr : attrs)
			assert parameters.containsKey(attr.getName()) : "value for parameter " + attr.getName() + " is missing";

		OpenAPIAction action = new OpenAPIAction(fillingFunction);
		action.addParameters(parameters);

		List<String> res = (List<String>) OpenAPIConnector.readEnums(action, this.fillingFunctionKey);
		System.out.println("received enums: " + res);
		return res;

	}

	@Deprecated
	private void update(DialogueGoal goal) {

		assert this.fillingFunction != null;
		assert this.fillingFunction.hasFrameGeneratedAttribute();
		System.out.println("update by frame " + goal.getFrame().getName());

		Collection<ServiceFunctionAttribute> attrs = this.fillingFunction.getFrameGeneratedAttributes();
		OpenAPIAction action = new OpenAPIAction(fillingFunction);
		for (ServiceFunctionAttribute attr : attrs) {
			if (attr.isFrameGenerated()) {

				Slotable node = goal.getNode(attr.getSlotName());
				if (node == null) {
					System.out.println("no node named " + attr.getSlotID() + " found");
				} else {
					if (node instanceof Fillable) {
						Fillable fill = (Fillable) node;
						System.out.println("fill " + attr.getName() + " with " + fill.getValue());
						action.addParameter(attr, fill.getValue());
					}
				}

			}
		}

		List<String> retrievedEnums = (List<String>) OpenAPIConnector.readEnums(action, this.fillingFunctionKey);
		System.out.println("enums: " + retrievedEnums);
		if (retrievedEnums != null && !retrievedEnums.isEmpty())
			this.enumList = retrievedEnums;
	}

	public List<String> getUpdatedEnumList() {

		System.out.println("get updated enum list " + this.getName());
		List<String> res = this.update();
		if (res != null)
			return res;

		if (this.enumList != null) {
			System.out.println("return old");
			return this.enumList;
			
		}

		return new ArrayList<String>();
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

		if (this.fillingFunction != null)
			return fillingFunction;

		if (this.contentURL != null)
			return new ServiceFunction(contentURL);

		return null;
	}

	public void setFillingFunction(ServiceFunction retrieveFunction) {
		this.fillingFunction = retrieveFunction;
	}

	public void setFillingFunctionKey(String key) {
		this.fillingFunctionKey = key;
	}

	public String getRetrieveFunctionKey() {
		return this.fillingFunctionKey;
	}

	public boolean isFile() {
		return this.file != null;
	}

	public MessageFile getFile() {
		return this.file;
	}

	public void setFile(MessageFile file) {
		this.file = file;
	}

	public void setEntity(IntentEntity entity) {
		this.entityKeyword = entity.getEntityKeyword();
	}

	public ParameterInput getInput() {
		return input;
	}

	public void setInput(ParameterInput input) {
		this.input = input;
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

	public String getContentFill() {
		return contentFill;
	}

	public void setContentFill(String contentFill) {
		this.contentFill = contentFill;
	}

	public boolean hasDynamicEnums() {
		if (this.contentURL != null)
			return true;
		if (this.getRetrieveFunction() != null)
			return true;
		return false;
	}

	public String getSlotID() {

		return slotID;

	}

	public void setSlotID(String slotID) {
		System.out.println("set slot id " + slotID + " of " + this.getIdName());
		if (slotID != null)
			this.slotID = slotID;
	}

	public void setSlotName(String slotName) {
		System.out.println("set slot name " + slotName);
		this.slotName = slotName;
	}

	public String getSlotName() {
		if (this.slotName == null || this.slotName.contentEquals(""))
			return null;
		return this.slotName;
	}

	/**
	 * Return attributes that this attribute is dependent on by dynamic enums. The
	 * (if possible values of this attribute depend on a retrieve Function call,
	 * that has attributes itself)
	 * 
	 * @return list of attributes
	 */
	public Collection<ServiceFunctionAttribute> getOpenAttributes() {

		if (!this.hasDynamicEnums() || this.getRetrieveFunction() == null)
			return new ArrayList<>();

		Collection<ServiceFunctionAttribute> res = new ArrayList<>();
		ServiceFunction function = this.getRetrieveFunction();
		for (ServiceFunctionAttribute fa : function.getAllAttributes()) {
			if (fa.isOpen())
				res.add(fa);
		}

		return res;
	}

	/**
	 * Indicate if this attribute already knows how it is filled with a value
	 * 
	 * @return can be filled (true) or not (false)
	 */
	public boolean isOpen() {

		System.out.println("is open? " + this.getName() + ", content:" + this.hasContent() + ", frameg: "
				+ this.isFrameGenerated());

		if (this.hasContent())
			return false;

		return true;
	}

	/**
	 * Indicates if this parameter is already filled with static content
	 * 
	 * @return is filled (true) or not (false)
	 */
	public boolean hasContent() {
		return this.content != null && !this.content.contentEquals("");
	}

	/**
	 * Indicate if this parameter should be filled with the value of another
	 * parameter of a preceding frame.
	 * 
	 * @return is filled by frame parameter (true) or not (false)
	 */
	public boolean isFrameGenerated() {
		System.out.println("is frame generated? " + this.getId() + " " + this.getIdName() + " slotID: " + this.slotID
				+ " slotName: " + this.slotName);
		if (this.slotID != null && !this.slotID.contentEquals(""))
			return true;
		if (this.slotName != null && !this.slotName.contentEquals(""))
			return true;
		return false;
	}

	public ServiceFunctionAttribute merge(ServiceFunctionAttribute attr) {
		assert attr != null;

		System.out.println("Merge " + attr.getName() + " into " + this.getName() + " slotId: " + attr.getSlotID());
		System.out.println("Merge " + attr.getId() + " into " + this.getId() + " format: " + attr.getFormat());

		if (attr.getId() != null && !attr.getId().contentEquals("")) {
			this.id = attr.getId();
			System.out.println("setting id to " + this.id);
		}

		if (attr.getParameterType() != null)
			this.parameterType = attr.getParameterType();

		if (attr.getContentURL() != null)
			this.setContentURL(attr.getContentURL());
		if (attr.getContentURLKey() != null)
			this.setContentURLKey(attr.getContentURLKey());
		if (attr.getContent() != null)
			this.setContent(attr.getContent());
		if (attr.getContentFill() != null)
			this.setContentFill(attr.getContentFill());
		if (attr.getRetrieveFunction() != null)
			this.setFillingFunction(attr.getRetrieveFunction());
		if (attr.getRetrieveFunctionKey() != null)
			this.setFillingFunctionKey(attr.getRetrieveFunctionKey());

		if (attr.getContentType() != null)
			this.setContentType(attr.getContentType());
		if (attr.getEntity() != null)
			this.setEntity(new IntentEntity(attr.getEntity()));
		if (attr.getSlotID() != null)
			this.setSlotID(attr.getSlotID());
		if (attr.getSlotName() != null)
			this.setSlotName(attr.getSlotName());
		if (attr.getFile() != null)
			this.setFile(attr.getFile());

		if (attr.hasDynamicEnums())
			this.setContentType("enum");
		if (attr.getFormat() != null)
			this.format = attr.getFormat();
		if (attr.getInput() != null)
			this.input = attr.getInput();

		return this;

	}

	public int getMinItems() {
		return minItems;
	}

	public void setMinItems(int minItems) {
		this.minItems = minItems;
	}

	public String prettyPrint(int i) {
		String res = "";
		for(int j=0; j<i; j++) {
			res = res + "#";
		}
		res = res + this.getName() + " " + this.getDiscriminator();
		for(ServiceFunctionAttribute attr : this.childAttributes)
			res = res + "\n" + attr.prettyPrint(i+1);
		
		return res;
	}

}
