package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.List;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import net.minidev.json.JSONObject;

public class DialogueGoal {

    private RootNode root;

    public DialogueGoal(Frame frame) {

	assert frame != null;

	this.root = new RootNode(frame);

	invariant();
    }

    /**
     * @return true if all required slots are filled.
     */
    public boolean isReady() {

	invariant();

	return this.root.isReady();
    }

    /**
     * @return true if all slots are filled.
     */
    public boolean isFull() {

	invariant();

	return this.root.isFull();
    }

    /**
     * @param slot
     * @param value
     * @return true if slot was filled. false if value does not match the slot.
     */
    public boolean fill(Fillable node, String value) {

	assert node != null : "node parameter is null";
	assert value != null : "value parameter is null";
	invariant();

	node.fill(value);

	return true;
    }

    /**
     * @param slot
     * @return true if slot was deleted. false if slot was not filled.
     */
    public boolean delete(Fillable node) {

	assert node != null : "slot parameter is null";
	invariant();

	node.clear();

	return true;
    }

    /**
     * @param slot
     * @return true if slot is part of this goal.
     */
    public boolean contains(Node node) {

	assert node != null : "node parameter is null";
	invariant();

	return this.root.getAll().contains(node);
    }

    public boolean contains(Slot slot) {

	assert slot != null : "slot parameter is null";
	assert getFrame() != null : "frame of dialogue goal is null";
	if (getFrame().getDescendants() == null)
	    return false;
	return getFrame().getDescendants().contains(slot);
    }

    public boolean contains(String name) {

	assert name != null : "name parameter is null";
	assert getFrame() != null : "frame of dialogue goal is null";
	if (getFrame().getDescendants() == null)
	    return false;
	return getFrame().getDescendants().contains(name);
    }

    /**
     * @param slot
     * @return true if slot is filled.
     */
    public boolean isFilled(Fillable node) {

	invariant();
	assert node != null : "node parameter is null";
	assert this.contains((Node) node) : "slot is not part of this goal";

	return node.isFilled();
    }

    public Slot getSlot(String name) {

	assert name != null : "name parameter is null";
	assert this.contains(name) : "slot is not contained in frame";

	return getFrame().getSlot(name);
    }

    public Slotable getNode(String name) {

	assert name != null : "name parameter is null";
	invariant();
	assert this.root.getAll() != null : "null nodes in tree";
	assert this.contains(name) : "node is not contained in frame";

	return this.root.getAll().get(name);
    }

    public Fillable getFillable(String name) {

	assert name != null : "name parameter is null";
	invariant();
	assert this.root.getAll() != null : "null nodes in tree";
	assert this.contains(name) : "node is not contained in frame";

	return (Fillable) this.root.getAll().Fillables().get(name);

    }

    public String getValue(Fillable node) {

	assert node != null : "node parameter is null";
	assert this.contains((Node) node) : "node is not contained";

	return node.getValue();

    }


    /**
     * @return a slot that is not filled yet.
     */
    public Node next() {

	invariant();
	assert !this.isFull() : "next node of full node tree";

	return root.next();
    }

    public void reset() {

	invariant();
	this.root = new RootNode(root.getFrame());
    }

    public Frame getFrame() {
	return this.root.getFrame();
    }

    public DialogueAct getReqConfAct() {

	invariant();
	assert this.isReady() : "goal is not ready yet";

	// ntent and entities
	DialogueAct act = new DialogueAct();
	act.setIntent(getFrame().getReqConfIntent());
	List<Fillable> values = root.getAll().getFilledValues();
	for (Fillable node : values)
	    act.addEntity(node.getSlot().getName(), node.getValue());

	// default message
	String message = "We have all necessary data \n";
	for (Map.Entry<String, String> entry : act.getEntities().entrySet())
	    message = message.concat(entry.getKey()).replaceAll("_", " ").concat(": \t ").concat(entry.getValue())
		    .concat(" \n");
	message = message.concat("is this right? \n");
	act.setMessage(message);

	// expected input
	ExpectedInput input = new ExpectedInput();
	input.setIntend(getFrame().getConfirmIntent());
	input.setType(InputType.Confirmation);
	act.setExpected(input);
	return (act);
    }

    public DialogueAct getReqOptionalAct() {

	invariant();
	assert this.isReady() : "goal is not ready yet";

	// ntent and entities
	DialogueAct act = new DialogueAct();

	// default message
	String message = "There are more optional parameter. \n Do you want to fill them?";
	act.setMessage(message);

	// expected input
	ExpectedInput input = new ExpectedInput();
	input.setIntend(getFrame().getConfirmIntent() + "_optional");
	input.setType(InputType.Confirmation);
	act.setExpected(input);
	return (act);
    }

    public DialogueAct getRequestAct(Slot slot) {

	assert slot != null : "slot parameter is null";
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

    public DialogueAct getInformAct(Node node) {

	assert node != null : "node parameter is null";
	assert node instanceof Fillable : "node is not fillable";
	assert this.contains(node) : "slot is not contained in frame";

	Slot slot = ((Fillable) node).getSlot();
	DialogueAct act = new DialogueAct();
	act.setIntent(slot.getInformIntent());
	act.addEntity(slot.getEntity(), ((Fillable) node).getValue());

	return act;
    }

    public DialogueAct getConfirmAct(Node node) {

	assert node != null : "node parameter is null";
	invariant();
	assert node.isFull() : "slot is not filled yet";

	Slot slot = ((Fillable) node).getSlot();
	DialogueAct act = new DialogueAct();
	act.setIntent(slot.getConfirmIntent());
	act.addEntity(slot.getName(), ((Fillable) node).getValue());

	ExpectedInput input = new ExpectedInput();
	input.setIntend(slot.getConfirmIntent());
	input.setType(InputType.Confirmation);
	return (act);
    }

    public ServiceFunction getServiceAction() {

	ServiceFunction template = getFrame().getServiceFunction();
	ServiceFunction action = new ServiceFunction();
	action.setActionType(template.getActionType());
	action.setConsumes(template.getConsumes());
	action.setFunctionName(template.getFunctionName());
	action.setFunctionPath(template.getFunctionPath());
	action.setHttpMethod(template.getHttpMethod());
	action.setProduces(template.getProduces());
	action.setServiceName(template.getServiceName());
	if (template.hasAttributes())
	    for (ServiceFunctionAttribute attr : template.getAttributes())
		action.addAttribute(copy(attr));
	return action;

    }

    private ServiceFunctionAttribute copy(ServiceFunctionAttribute template) {
	System.out.println(template.getName());
	ServiceFunctionAttribute attr = new ServiceFunctionAttribute();
	attr.setContentType(template.getContentType());
	attr.setName(template.getName());
	attr.setParameterType(template.getParameterType());
	// if (this.getFillable(template.getName()) != null)
	// attr.setContent(this.getFillable(template.getName()).getValue());
	if (template.hasChildren())
	    for (ServiceFunctionAttribute child : template.getChildAttributes())
		attr.addChildAttribute(copy(child));

	return attr;
    }

    public void print() {
	this.root.getAll().print();
    }

    public RootNode getRoot() {
	return this.root;
    }

    public void invariant() {
	assert this.root != null : "no root node";
    }

    public JSONObject toJSON() {

	JSONObject res = new JSONObject();

	return res;
    }

}
