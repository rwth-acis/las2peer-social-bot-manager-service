package i5.las2peer.services.socialBotManagerService.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInputType;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.SlotSet;

public class Slot {

    /**
     * name of this slot
     */
    final String name;

    /**
     * intent that corresponds to the intent recognition of the NLU module
     */
    String nluIntent;

    /**
     * intent that corresponding to the message creation of the NLG module
     */
    String nlgIntent;

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
     * The message that the bot can send to the user to ask for the information
     * about this slot
     */
    String message;

    /**
    * 
     */
    ExpectedInputType inputType;

    /**
     * The service parameter that the value of this slot should fill
     */
    ServiceFunctionAttribute parameter;

    /**
     * children of this slot
     **/
    List<Slot> children;

    public Slot(String name) {
	this.name = name;
	this.children = new SlotSet();
    }

    /**
     * @param value
     * @return true if the input value is acceptable for this slot
     */
    public boolean validate(String value) {

	assert this.inputType != null : "no inputType defined.";

	if (this.inputType == null)
	    this.inputType = ExpectedInputType.Free;

	if (this.getInputType() == ExpectedInputType.Enum) {
	    if (!this.getParameter().getEnumList().contains(value))
		return false;
	}

	return this.inputType.validate(value);
    }

    public String getName() {
	return name;
    }

    public String getEntity() {
	return entity;
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

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
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

    public String getNlu_intent() {
	return nluIntent;
    }

    public void setNlu_intent(String nlu_intent) {
	this.nluIntent = nlu_intent;
    }

    public String getNlg_intent() {
	return nlgIntent;
    }

    public void setNlg_intent(String nlg_intent) {
	this.nlgIntent = nlg_intent;
    }

    public List<Slot> getChildren() {
	return children;
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
	System.out.println("slot " + this.getName() + " add child " + slot.getName());
	this.children.add(slot);
    }

    public Collection<? extends Slot> getDescendants() {
	Collection<Slot> desc = new SlotSet();
	desc.add(this);
	if (this.hasChildren())
	    for (Slot slot : this.getChildren()) {
		desc.addAll(slot.getDescendants());
	    }
	return desc;
    }

    @Override
    public String toString() {
	String res = "Slot ".concat(this.getName());
	if (this.isEntity_extraction()) {
	    res = res.concat(" entity: ").concat(this.getEntity());
	}
	res = res.concat(" Selection ".concat(Boolean.toString(this.isSelection())));
	res = res.concat(" children: ");
	for (Slot slot : this.children) {
	    res = res.concat(slot.toString());
	}
	return res;
    }

    public String getNluIntent() {
	return nluIntent;
    }

    public void setNluIntent(String nluIntent) {
	this.nluIntent = nluIntent;
    }

    public String getNlgIntent() {
	return nlgIntent;
    }

    public boolean hasParameter() {
	return this.parameter != null;
    }

    public void setNlgIntent(String nlgIntent) {
	this.nlgIntent = nlgIntent;
    }

    public ExpectedInputType getInputType() {
	return inputType;
    }

    public void setInputType(ExpectedInputType inputType) {
	this.inputType = inputType;
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

    public SlotSet getRequired() {
	SlotSet slots = new SlotSet();
	if (this.isSelection()) {
	    slots.add(this);
	    return slots;
	}

	if (this.isLeaf() && this.isRequired()) {
	    slots.add(this);
	    return slots;
	}

	if (this.hasChildren()) {
	    for (Slot slot : this.getChildren()) {
		slots.addAll(slot.getRequired());
	    }
	}
	return slots;
    }

    public SlotSet getRequired(String branch) {
	SlotSet slots = new SlotSet();
	if (!this.isSelection()) {
	    return getRequired();
	}

	if (this.hasChildren()) {
	    for (Slot slot : this.getChildren()) {
		if (slot.getEntity() != null && slot.getEntity().contentEquals(branch))
		    slots.addAll(slot.getRequired());
	    }
	}

	return slots;
    }

    public Slot getChild(String name) {
	if (this.hasChildren()) {
	    for (Slot slot : this.getChildren()) {
		if (slot.getName().contentEquals(name))
		    return slot;
	    }
	}
	return null;
    }

    public boolean isReady(Map<Slot, String> state) {
	// One child is ready
	if (this.isSelection()) {
	    for (Slot slot : this.getChildren()) {
		if (slot.isReady(state))
		    return true;
	    }
	    return false;
	}

	// all children are ready
	if (this.hasChildren()) {
	    for (Slot slot : this.getChildren()) {
		if (!slot.isReady(state))
		    return false;
	    }
	    return true;
	}

	// this is ready
	return state.containsKey(this.name);
    }

    public Collection<? extends Slot> getDescendants(Map<Slot, String> values) {
	Collection<Slot> desc = new SlotSet();
	desc.add(this);

	if (this.isSelection()) {
	    for (Slot slot : this.getChildren()) {
		if (slot.getEntity().contentEquals(values.get(this))) {
		    desc.addAll(slot.getDescendants(values));
		    return desc;
		}
	    }
	}

	if (this.hasChildren())
	    for (Slot slot : this.getChildren()) {
		desc.addAll(slot.getDescendants(values));
	    }
	return desc;
    }

}
