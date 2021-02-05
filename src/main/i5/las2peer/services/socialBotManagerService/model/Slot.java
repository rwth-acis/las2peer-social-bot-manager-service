package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.SlotList;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIResponse;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ParameterType;

/**
 * Element of a Frame Connects Frames with ServiceFunctionAttributes
 */
public class Slot {

	/**
	 * name of this slot
	 */
	final String name;

	/**
	 * entity identification corresponding to the entity recognition of the NLU
	 * module
	 */
	String entity;

	/**
	 * Identifies if this slot has to be filled during a conversation or if it is
	 * optional
	 */
	boolean required;

	/**
	 * Identifies if only one of its children should be filled (TRUE) or not (FALSE)
	 *
	 */
	boolean selection;

	/**
	 * Identifies if this slot is filled by entity extraction or by free direct user
	 * input
	 */
	boolean entity_extraction;

	/**
	 * Identifies the priority with which this slot must be filled. A low number
	 * implies a high priority.
	 */
	int priority;

	/**
	 * Defines which values are valid to fill this slot.
	 */
	InputType inputType;

	/**
	 * The service parameter that the value of this slot should fill
	 */
	ServiceFunctionAttribute parameter;

	/**
	 * children of this slot
	 **/
	List<Slot> children;

	/**
	 * The message that the bot can send to the user to ask for the information
	 * about this slot
	 */
	String requestMessage;

	public Slot(String name) {
		this.name = name;
		this.children = new SlotList();
		this.priority = 0;
	}

	public Slot(ServiceFunctionAttribute attr) {
		this(attr.getName());
		this.parameter = attr;
		this.inputType = InputType.fromString(attr.getContentType());
	}

	public Slot(String id, String name, ParameterType type) {
		this(new ServiceFunctionAttribute(id, name, type));
	}

	public Slot(String name, ParameterType type) {
		this(new ServiceFunctionAttribute(name, name, type));
	}

	/**
	 * @param value
	 * @return true if the input value is acceptable for this slot
	 */
	public boolean validate(String value) {

		assert this.inputType != null : "no inputType defined.";

		if (this.inputType == null)
			this.inputType = InputType.Free;

		if (this.getInputType() == InputType.Enum) {
			List<String> enumList = this.getParameter().getEnumList();
			if (enumList.isEmpty())
				return true;

			if (!enumList.contains(value))
				return false;

		}

		if (!this.inputType.validate(value))
			return false;
		
		if (this.hasParameter() && this.parameter.getInput() != null) {
			if(!this.parameter.getInput().validate(value))
				return false;
		}
		
		return true;
	}

	/**
	 * UUID
	 * 
	 * @return
	 */
	public String getID() {
		if (this.parameter != null && this.parameter.getId() != null)
			return this.parameter.getId();
		return this.name;
	}

	/**
	 * Name for unique identification inside frame
	 * 
	 * @return
	 */
	public String getName() {

		return this.name;
	}

	/**
	 * Name corresponding to OpenAPI specification parameter name
	 * 
	 * @return
	 */
	public String getAPIName() {
		if (this.hasParameter())
			return this.parameter.getName();
		return this.name;
	}

	/**
	 * Human readable name to display during conversation
	 * 
	 * @return
	 */
	public String getDisplayName() {
		String[] parts = this.name.split("_");
		if (parts.length == 1)
			return parts[0];
		if (parts.length == 2)
			return (parts[0] + " " + parts[1]);
		int l = parts.length;
		return (parts[l - 2] + " " + parts[l - 1]);
	}

	public String getEntity() {

		if (entity != null)
			return entity;

		if (this.getParameter() != null && this.getParameter().getEntity() != null)
			return this.getParameter().getEntity();

		if (this.getEnumList() != null)
			return this.getName();

		return null;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getRequestMessage() {
		return requestMessage;
	}

	public void setRequestMessage(String message) {
		this.requestMessage = message;
	}

	public ServiceFunctionAttribute getParameter() {
		return parameter;
	}

	public void setParameter(ServiceFunctionAttribute parameter) {
		this.parameter = parameter;
	}

	protected boolean isEntity_extraction() {
		return entity_extraction;
	}

	protected void setEntity_extraction(boolean entity_extraction) {
		this.entity_extraction = entity_extraction;
	}

	public List<Slot> getChildren() {
		return children;
	}

	/**
	 * @param i priority of requested children
	 * @return
	 */
	public List<Slot> getChildren(int i) {
		if(i != 0)
		System.out.println("get Children of priority " + i);
		List<Slot> res = new ArrayList<>();
		
		for (Slot child : this.children) {
			System.out.println(child.getName() + " " + child.getPriority());
			if (child.getPriority() == i)
				res.add(child);
		}
		
		return res;
	}

	public void setChildren(List<Slot> children) {
		this.children = children;
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	public boolean isLeaf() {
		return !this.hasChildren();
	}

	public void addChild(Slot slot) {
		this.children.add(slot);
	}

	public Collection<? extends Slot> getDescendants() {
		Collection<Slot> desc = new SlotList();
		desc.add(this);
		if (this.hasChildren())
			for (Slot slot : this.getChildren())
				desc.addAll(slot.getDescendants());
		return desc;
	}

	@Override
	public String toString() {
		String res = "Slot ".concat(this.getName());
		if (this.isEntity_extraction())
			res = res.concat(" entity: ").concat(this.getEntity());
		res = res.concat(" Selection ".concat(Boolean.toString(this.isSelection())));
		res = res.concat(" children: ");
		for (Slot slot : this.children)
			res = res.concat(slot.toString());
		return res;
	}

	public boolean hasParameter() {
		return this.parameter != null;
	}

	public InputType getInputType() {
		return inputType;
	}

	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}

	/**
	 * Possible intents generated by this slot
	 * 
	 * @return
	 */
	public Collection<String> getIntents() {
		List<String> res = new ArrayList<>(5);
		res.add(this.getInformIntent());
		res.add(this.getConfirmIntent());
		res.add(this.getDenyIntent());
		res.add(this.getRequestIntent());
		res.add(this.getReqConfIntent());
		return res;
	}

	public String getReqConfIntent() {
		return "reqconf_" + name + "_proceed";
	}

	public String getInformIntent() {
		return "inform_" + name;
	}

	public String getRequestIntent() {
		return "request_" + name;
	}

	public String getConfirmIntent() {
		return "confirm_" + name;
	}

	public String getDenyIntent() {
		return "deny_" + name;
	}

	public boolean isSelection() {
		return selection;
	}

	public void setSelection(boolean selection) {
		this.selection = selection;
	}

	public List<String> getEnumList() {
		return this.getParameter().getUpdatedEnumList();
	}

	public boolean hasEnumList() {

		if (getParameter() == null || getParameter().getEnumList() == null)
			return false;

		List<String> enumList = this.getParameter().getEnumList();
		return (enumList != null);
	}

	public boolean isArray() {
		if (this.hasParameter())
			return this.getParameter().isArray();
		return false;
	}

	public void setArray(boolean value) {
		if (!this.hasParameter())
			return;

		this.getParameter().setArray(value);

	}

	public SlotList getRequired() {
		SlotList slots = new SlotList();
		if (this.isSelection()) {
			slots.add(this);
			return slots;
		}

		if (this.isLeaf() && this.isRequired()) {
			slots.add(this);
			return slots;
		}

		if (this.hasChildren())
			for (Slot slot : this.getChildren())
				slots.addAll(slot.getRequired());
		return slots;
	}

	public SlotList getRequired(String branch) {
		SlotList slots = new SlotList();
		if (!this.isSelection())
			return getRequired();

		if (this.hasChildren())
			for (Slot slot : this.getChildren())
				if (slot.getEntity() != null && slot.getEntity().contentEquals(branch))
					slots.addAll(slot.getRequired());

		return slots;
	}

	public Slot getChild(String name) {
		if (this.hasChildren())
			for (Slot slot : this.getChildren())
				if (slot.getName().contentEquals(name))
					return slot;
		return null;
	}

	public boolean isReady(Map<Slot, String> state) {
		// One child is ready
		if (this.isSelection()) {
			for (Slot slot : this.getChildren())
				if (slot.isReady(state))
					return true;
			return false;
		}

		// all children are ready
		if (this.hasChildren()) {
			for (Slot slot : this.getChildren())
				if (!slot.isReady(state))
					return false;
			return true;
		}

		// this is ready
		return state.containsKey(this.name);
	}

	public Collection<? extends Slot> getDescendants(Map<Slot, String> values) {

		assert values != null : "values are null";

		Collection<Slot> desc = new SlotList();
		desc.add(this);

		if (this.isSelection() && this.hasChildren())
			for (Slot slot : this.getChildren()) {
				assert slot != null : "child slot is null";
				if (slot.getEntity() != null && values.containsKey(this)
						&& slot.getEntity().contentEquals(values.get(this))) {
					desc.addAll(slot.getDescendants(values));
					return desc;
				}
			}

		if (this.hasChildren())
			for (Slot slot : this.getChildren())
				desc.addAll(slot.getDescendants(values));
		return desc;
	}

	/**
	 * Returns parameter type
	 * 
	 * @return parameter type
	 */
	public ParameterType getParameterType() {
		assert this.parameter != null : "slot has no assigned function parameter";

		return this.parameter.getParameterType();
	}

	/**
	 * Indicate if this slot is filled by a enumeration that is retrieved by a
	 * function call
	 * 
	 * @return filled by retrieved enums (true) or not (false)
	 */
	public boolean hasDynamicEnums() {
		if (!this.hasParameter())
			return false;
		return this.parameter.hasDynamicEnums();
	}

	public List<String> getUpdatedEnumList(Map<String, String> parameters) {

		if (!this.hasParameter())
			return new ArrayList<>();
				
		return this.getParameter().getUpdatedEnumList(parameters);

	}

	public boolean hasDynamicFormat() {

		System.out.println(" check dynamic format " + this.getParameter().getFormat());

		if (!this.hasParameter())
			return false;

		if (this.getParameter().getFormat() == null) {
			System.out.println("parameter " + this.getName() + " has no format");
			return false;
		}

		ServiceFunctionAttribute para = this.getParameter();
		if (para.getRetrieveFunction() == null)
			return false;
		if (para.getFormat().startsWith("/"))
			return true;
		return false;
	}

	public String getUpdatedFormat(Map<String, String> parameters) {

		assert this.parameter != null : "slot has no parameter";

		ServiceFunctionAttribute para = this.getParameter();
		if (!this.hasDynamicFormat())
			return para.getFormat();

		if (parameters == null)
			parameters = new HashMap<>();

		ServiceFunction function = para.getRetrieveFunction();
		assert function != null;

		Collection<ServiceFunctionAttribute> attrs = function.getFrameGeneratedAttributes();
		for (ServiceFunctionAttribute attr : attrs)
			assert parameters.containsKey(attr.getName()) : "value for parameter " + attr.getName() + " is missing";

		OpenAPIAction action = new OpenAPIAction(function);
		action.addParameters(parameters);

		OpenAPIResponse response = action.execute();
		if (response.isError())
			return para.getFormat();

		String keyFilter = para.getFormat().replaceFirst("/", "");
		Collection<String> res = OpenAPIConnector.searchValuesByKey(response.getAsJSON(), keyFilter);
		System.out.println("searched for dynamic input format by key " + keyFilter + " found " + res.size());

		for (String value : res)
			if (InputType.fromString(value) != InputType.Free)
				return value;
		for (String value : res)
			return value;

		return para.getFormat();

	}

	/**
	 * Give a list of parameters that have to be filled before this slot can be
	 * filled.
	 * 
	 * @return
	 */
	public Collection<ServiceFunctionAttribute> getOpenSlots() {
		assert this.parameter != null;

		if (!this.hasDynamicEnums())
			return new ArrayList<>();

		ServiceFunctionAttribute attr = this.getParameter();
		return attr.getOpenAttributes();
	}

	/**
	 * Update dynamic enums of this slot
	 */
	public void update() {

		if (!this.hasDynamicEnums())
			return;

		List<String> list = this.parameter.getUpdatedEnumList();
		assert list != null;
		System.out.println("slot " + this.getName() + " updated list: " + list.size());
	}

	public String prettyPrint(int i) {
		String res = "";
		for (int j = 0; j < i; j++) {
			res = res + "#";
		}
		res = res + this.getName();
		for (Slot slot : this.children)
			res = res + "\n" + slot.prettyPrint(i + 1);

		return res;
	}

}
