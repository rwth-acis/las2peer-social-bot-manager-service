package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInputType;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class DialogueGoal {

    Frame frame;
    Map<Slot, String> values;

    public DialogueGoal(Frame frame) {
	this.values = new HashMap<Slot, String>();
	this.frame = frame;
    }

    /**
     * @return true if all required slots are filled.
     */
    public boolean isReady() {

	assert this.frame != null : "frame of dialogue goal is null";
	assert this.values != null : "slot value map of dialogue goal is null";

	for (Slot slot : frame.getRequired(this.values)) {

	    if (!this.values.containsKey(slot)) {
		if (slot.isLeaf()) {
		    System.out.println("not full. key not contained: " + slot.getName());
		    return false;
		}
		if (slot.isSelection()) {
		    System.out.println("not full. key not contained: " + slot.getName());
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * @return true if all slots are filled.
     */
    public boolean isFull() {

	assert this.frame != null : "frame of dialogue goal is null";
	assert this.values != null : "slot value map of dialogue goal is null";

	for (Slot slot : frame.getDescendants(this.values)) {
	    if (!this.values.containsKey(slot)) {
		if (slot.isLeaf()) {
		    System.out.println("not full. key not contained: " + slot.getName());
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * @param slot
     * @param value
     * @return true if slot was filled. false if value does not match the slot.
     */
    public boolean fill(Slot slot, String value) {

	assert this.values != null : "slot value map of dialogue goal is null";
	assert slot != null : "slot parameter is null";
	assert value != null : "value parameter is null";

	// check input type
	ExpectedInputType type = slot.getInputType();
	if (!type.validate(value)) {
	    System.out.println("input does not match type: " + type);
	    return false;
	}

	// fill slot
	if (this.values.containsKey(slot)) {
	    if (slot.getParameter().isArray()) {
		value = values.get(slot).concat(", ").concat(value);
	    }
	    System.out.println("override slot " + slot.getName() + " value: " + values.get(slot) + " with " + value);
	}
	this.values.put(slot, value);
	System.out.println("fill slot value " + slot.getName() + " " + value);

	return true;
    }

    /**
     * @param slot
     * @return true if slot was deleted. false if slot was not filled.
     */
    public boolean delete(Slot slot) {

	assert this.values != null : "slot value map of dialogue goal is null";
	assert slot != null : "slot parameter is null";

	if (!this.values.containsKey(slot)) {
	    System.out.println("slot " + slot.getName() + " is not contained");
	    return false;
	}

	this.values.remove(slot);
	return true;
    }

    /**
     * @param slot
     * @return true if slot is part of this goal.
     */
    public boolean contains(Slot slot) {

	assert this.frame != null : "frame of dialogue goal is null";
	if (this.frame.getDescendants() == null)
	    return false;
	return this.frame.getDescendants().contains(slot);
    }

    public boolean contains(String name) {

	assert this.frame != null : "frame of dialogue goal is null";
	if (this.frame.getDescendants() == null)
	    return false;
	return frame.getDescendants().contains(name);
    }

    /**
     * @param slot
     * @return true if slot is filled.
     */
    public boolean isFilled(Slot slot) {

	assert this.frame != null : "frame of dialogue goal is null";
	assert this.values != null : "slot value map of dialogue goal is null";
	assert slot != null : "slot parameter is null";
	assert this.contains(slot) : "slot is not part of this goal";

	return this.values.containsKey(slot);
    }

    public Slot getSlot(String name) {

	assert this.frame != null : "frame of dialogue goal is null";
	assert this.contains(name) : "slot is not contained in frame";

	return frame.getSlot(name);
    }

    public String getValue(Slot slot) {

	assert this.frame != null : "frame of dialogue goal is null";
	assert this.contains(slot) : "slot is not contained in frame";

	if (this.values.containsKey(slot))
	    return this.values.get(slot);
	return null;

    }

    public Map<Slot, String> getValues() {
	return this.values;
    }

    /**
     * @return a slot that is not filled yet.
     */
    public Slot next() {

	System.out.println("next() " + frame.getRequired());

	for (Slot slot : frame.getRequired()) {

	    if (!this.values.containsKey(slot)) {
		if (slot.isLeaf())
		    return slot;
		if (slot.isSelection())
		    return slot;
	    }

	    if (slot.isSelection() && this.values.containsKey(slot)) {
		if (!slot.isReady(values)) {
		    for (Slot subSlot : slot.getRequired(this.values.get(slot))) {
			if (!this.values.containsKey(subSlot)) {
			    if (subSlot.isLeaf())
				return subSlot;
			}
		    }
		}
	    }

	}

	for (Slot slot : frame.getDescendants()) {
	    if (!this.values.containsKey(slot)) {
		if (slot.isLeaf())
		    return slot;
	    }
	}

	return null;
    }

    public void reset() {
	this.values.clear();
    }

    public Frame getFrame() {
	return this.frame;
    }

    public DialogueAct getReqConfAct() {

	assert this.frame != null : "frame of dialogue goal is null";
	assert this.values != null : "slot value map of dialogue goal is null";
	assert this.isReady() : "goal is not ready yet";

	// ntent and entities
	DialogueAct act = new DialogueAct();
	act.setIntent(frame.getReqConfIntent());
	Map<Slot, String> values = this.getValues();
	for (Map.Entry<Slot, String> vs : values.entrySet()) {
	    act.addEntity(vs.getKey().getName(), vs.getValue());
	}

	// default message
	String message = "We have all necessary data \n";
	for (Map.Entry<String, String> entry : act.getEntities().entrySet())
	    message = message.concat(entry.getKey()).replaceAll("_", " ").concat(": \t ").concat(entry.getValue())
		    .concat(" \n");
	message = message.concat("is this right? \n");
	act.setMessage(message);

	// expected input
	ExpectedInput input = new ExpectedInput();
	input.setIntend(frame.getConfirmIntent());
	input.setType(ExpectedInputType.Confirmation);
	act.setExpected(input);
	return (act);
    }

    public DialogueAct getReqOptionalAct() {

	assert this.frame != null : "frame of dialogue goal is null";
	assert this.values != null : "slot value map of dialogue goal is null";
	assert this.isReady() : "goal is not ready yet";

	// ntent and entities
	DialogueAct act = new DialogueAct();

	// default message
	String message = "There are more optional parameter. \n Do you want to fill them?";
	act.setMessage(message);

	// expected input
	ExpectedInput input = new ExpectedInput();
	input.setIntend(frame.getConfirmIntent() + "_optional");
	input.setType(ExpectedInputType.Confirmation);
	act.setExpected(input);
	return (act);
    }

    public DialogueAct getRequestAct(Slot slot) {

	assert slot != null : "slot is null";
	assert this.contains(slot) : "slot is not contained in frame";

	// intent
	DialogueAct act = new DialogueAct();
	act.setIntent(slot.getRequestIntent());

	// default message
	String message = "What is the *" + slot.getName() + "* \n\n";
	if (slot.hasParameter()) {
	    ServiceFunctionAttribute parameter = slot.getParameter();
	    if (parameter.getDescription() != null)
		message = message.concat("description:\t" + parameter.getDescription() + "\n");
	    if (slot.getInputType() != null)
		message = message.concat("input:     \t" + slot.getInputType().toString() + "\n");
	    if (parameter.getExample() != null)
		message = message.concat("example:    \t" + parameter.getExample() + "\n");
	    if (parameter.getEnumList() != null && parameter.getEnumList().size() > 0) {
		message = message.concat("possible answers: ");
		for (Object enu : parameter.getEnumList())
		    message = message.concat(", ").concat((String) enu);
		slot.setEntity(parameter.getName());
	    }
	}
	act.setMessage(message);

	// expected input
	ExpectedInput input = new ExpectedInput();
	input.setType(slot.getInputType());
	input.setIntend(slot.getInformIntent());
	input.setEntity(slot.getEntity());
	act.setExpected(input);
	return act;

    }

    public DialogueAct getInformAct(Slot slot) {

	assert slot != null : "slot is null";
	assert this.contains(slot) : "slot is not contained in frame";

	DialogueAct act = new DialogueAct();
	act.setIntent(slot.getInformIntent());
	act.addEntity(slot.getEntity(), getValue(slot));

	return act;
    }

    public DialogueAct getConfirmAct(Slot slot) {

	assert this.frame != null : "frame of dialogue goal is null";
	assert this.values != null : "slot value map of dialogue goal is null";
	assert this.isFilled(slot) : "slot is not filled yet";

	DialogueAct act = new DialogueAct();
	act.setIntent(slot.getConfirmIntent());
	act.addEntity(slot.getName(), this.values.get(slot));

	ExpectedInput input = new ExpectedInput();
	input.setIntend(slot.getConfirmIntent());
	input.setType(ExpectedInputType.Confirmation);
	return (act);
    }

    public ServiceFunction getServiceAction() {

	ServiceFunction template = frame.getServiceFunction();
	ServiceFunction action = new ServiceFunction();
	action.setActionType(template.getActionType());
	action.setConsumes(template.getConsumes());
	action.setFunctionName(template.getFunctionName());
	action.setFunctionPath(template.getFunctionPath());
	action.setHttpMethod(template.getHttpMethod());
	action.setProduces(template.getProduces());
	action.setServiceName(template.getServiceName());
	if (template.hasAttributes()) {
	    for (ServiceFunctionAttribute attr : template.getAttributes()) {
		action.addAttribute(copy(attr));
	    }
	}
	return action;

    }

    private ServiceFunctionAttribute copy(ServiceFunctionAttribute template) {

	ServiceFunctionAttribute attr = new ServiceFunctionAttribute();
	attr.setContentType(template.getContentType());
	attr.setName(template.getName());
	attr.setParameterType(template.getParameterType());
	attr.setContent(this.values.get(template.getName()));
	if (template.hasChildren()) {
	    for (ServiceFunctionAttribute child : template.getChildAttributes()) {
		attr.addChildAttribute(copy(child));
	    }
	}

	return attr;
    }

}
