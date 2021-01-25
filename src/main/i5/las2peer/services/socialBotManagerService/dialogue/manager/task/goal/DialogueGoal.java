package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ResponseParseMode;
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
	 * @param value
	 * @return true if slot was filled. false if value does not match the slot.
	 */
	public boolean fill(Fillable node, String value) {

		assert node != null : "node parameter is null";
		assert value != null : "value parameter is null";
		invariant();

		System.out.println(node.getName() + " was filled with " + value);
		node.fill(value);

		return true;
	}

	/**
	 * @param value
	 * @return true if slot was filled. false if value does not match the slot.
	 */
	public boolean fill(Slot slot, String value) {

		assert slot != null : "slot parameter is null";
		assert value != null : "value parameter is null";
		assert this.contains(slot);
		invariant();

		String name = slot.getName();
		Fillable node = this.getFillable(name);
		this.fill(node, value);

		return true;
	}

	public boolean fill(String name, String value) {
		assert name != null;
		assert value != null;

		Fillable node = this.getFillable(name);

		if (node == null)
			return false;

		return this.fill(node, value);
	}

	/**
	 * @return true if slot was deleted. false if slot was not filled.
	 */
	public boolean delete(Fillable node) {

		assert node != null : "slot parameter is null";
		invariant();

		node.clear();

		return true;
	}

	/**
	 * @return true if slot was deleted. false if slot was not filled.
	 */
	public boolean delete(Slot slot) {

		assert slot != null : "slot parameter is null";
		assert this.contains(slot);
		invariant();

		String name = slot.getName();
		Fillable node = this.getFillable(name);
		node.clear();

		return true;
	}

	/**
	 * @param node
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
		assert this.getAll() != null : "node list is null";

		if (this.getAll() == null)
			return false;

		return this.getAll().contains(name);
	}

	/**
	 * @param node
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
		assert this.contains(name) : "node " + name + " is not contained in frame";

		return this.root.getAll().get(name);
	}

	public Fillable getFillable(String name) {

		assert name != null : "name parameter is null";
		invariant();
		assert this.root.getAll() != null : "null nodes in tree";

		if (!this.contains(name)) {
			System.out.println(name + " not contained in frame " + this.getAll().getFillableNodes().size());
			for (Fillable node : this.getAll().getFillableNodes()) {
				System.out.println(node.getName());
			}
		}

		assert this.contains(name) : "node " + name + " is not contained in frame " + this.getFrame().getName();

		return (Fillable) this.root.getAll().Fillables().get(name);

	}

	public Fillable getNodeByEntity(Entity entity) {

		assert entity != null;
		assert entity.getEntityName() != null;
		invariant();

		System.out.println("get node by entity " + entity.getEntityName());
		System.out.println("number of nodes in goal " + root.getAll().size());
		Collection<Fillable> nodes = root.getAll().getByEntity(entity.getEntityName());
		System.out.println("number of nodes with entity: " + nodes.size());
		Fillable res = null;
		int prio = -1;
		for (Fillable node : nodes) {
			if (node.getSlot().getPriority() > prio) {
				res = node;
				prio = node.getSlot().getPriority();
			}
		}
		return res;

	}

	public String getValue(Fillable node) {

		assert node != null : "node parameter is null";
		assert this.contains((Node) node) : "node is not contained";

		return node.getValue();

	}

	public String getValue(Slot slot) {

		assert slot != null : "slot parameter is null";
		assert this.contains(slot) : "node is not contained";

		String name = slot.getName();
		Fillable node = this.getFillable(name);
		return node.getValue();

	}

	/**
	 * @return a slot that is not filled yet.
	 */
	public Node next() {

		invariant();
		assert !this.isFull() : "next node of full node tree";

		Node node = root.next();
		if (!(node instanceof Fillable))
			return node;

		Fillable fillable = ((Fillable) node);

		if (!fillable.hasFrameGeneratedEnum())
			return node;

		System.out.println("check frame generated attributes of  " + fillable.getAPIName());
		ServiceFunctionAttribute attr = fillable.getSlot().getParameter();
		for (ServiceFunctionAttribute slotAttr : attr.getRetrieveFunction().getFrameGeneratedAttributes()) {

			Fillable input = null;
			if (this.contains(slotAttr.getSlotID()))
				input = this.getFillable(slotAttr.getSlotID());
			else if (this.contains(slotAttr.getSlotName()))
				input = this.getFillable(slotAttr.getSlotName());

			assert input != null : "cant find " + slotAttr.getSlotID() + " in " + this.getFrame().getName();

			if (!input.isFilled()) {
				System.out.println("found: " + input.getAPIName());
				return (Node) input;
			}
		}

		return node;

	}

	public Map<String, String> getFunctionParametersOfNode(Fillable node) {

		assert node != null : "node parameter is null";
		assert node.getSlot() != null : "node has no slot";
		System.out.println("get function parameters of node " + node.getName());

		Map<String, String> res = new HashMap<>();
		ServiceFunctionAttribute parameter = node.getSlot().getParameter();
		if (parameter == null)
			return res;

		ServiceFunction fillingFunction = parameter.getRetrieveFunction();
		if (fillingFunction == null)
			return res;

		Collection<ServiceFunctionAttribute> attrs = fillingFunction.getFrameGeneratedAttributes();
		OpenAPIAction action = new OpenAPIAction(fillingFunction);
		for (ServiceFunctionAttribute attr : attrs) {

			if (attr.isFrameGenerated()) {
				Slotable helperNode = null;
				if (attr.getSlotName() != null)
					helperNode = this.getNode(attr.getSlotName());
				else
					helperNode = this.getNode(attr.getSlotID());
				if (helperNode == null) {
					System.out.println("no node named " + attr.getSlotID() + " found");
				} else {
					if (helperNode instanceof Fillable) {
						Fillable fill = (Fillable) helperNode;
						System.out.println("fill " + attr.getName() + " with " + fill.getValue());
						res.put(attr.getIdName(), fill.getValue());
					}
				}
			}
		}

		return res;
	}

	public void reset() {

		invariant();
		this.root = new RootNode(root.getFrame());
		System.out.println("reset dialogue goal");
	}

	public Frame getFrame() {
		return this.root.getFrame();
	}

	public RootNode getRoot() {
		return this.root;
	}

	public NodeList getAll() {
		return this.root.getAll();
	}

	public OpenAPIAction getOpenAPIAction() {

		ServiceFunction function = this.getFrame().getServiceFunction();
		OpenAPIAction res = new OpenAPIAction(function);
		res.setBodyParameter(root.toBodyJSON());
		res.setPathParameters(root.getPathParameters());
		res.setQueryParameters(root.getQueryParameters());
		System.out.println("parameters " + this.getFrame().getName() + " path:" + root.getPathParameters().size());
		if (getFrame().getFile() != null && !getFrame().getFile().contentEquals(""))
			res.setResponseParseMode(ResponseParseMode.FILE);

		return res;
	}

	public List<String> getInputIntents() {

		List<String> res = new ArrayList<>();

		// Frame intents
		res.add(root.getFrame().getIntent());
		res.add(root.getFrame().getReqConfIntent());
		res.add(root.getFrame().getReqConfIntent() + "_optional");

		// Slot intents
		List<Slotable> nodes = root.getAll().Slotable();
		for (Slotable node : nodes) {
			res.add(node.getInformIntent());
		}

		// control intents
		res.add("revert");
		res.add("cancel");
		res.add("skip");

		return res;
	}

	public List<String> getOutputIntents() {

		List<String> res = new ArrayList<>();

		// Frame intents
		res.add(root.getFrame().getIntent());
		res.add(root.getFrame().getReqConfIntent());
		res.add(root.getFrame().getReqConfIntent() + "_optional");

		// Slot intents
		List<Slotable> nodes = root.getAll().Slotable();
		for (Slotable node : nodes) {
			res.add(node.getRequestIntent());
		}

		return res;
	}

	public Collection<Entity> getEnums(String utterance) {

		System.out.println("search for enum in utterance: " + utterance);
		List<Entity> res = new ArrayList<>();

		if (this.root.getChildren() == null)
			return res;

		for (Node node : this.root.getChildren()) {

			if (node instanceof Fillable) {
				Fillable fillable = (Fillable) node;
				if (fillable.getSlot().getEnumList() != null) {
					for (String enu : fillable.getSlot().getEnumList()) {
						if (utterance.toLowerCase().contains(enu.toLowerCase())) {
							System.out.println("found" + enu + " in " + fillable.getName());
							Entity entity = new Entity(fillable.getName(), enu);
							res.add(entity);
						}
					}
				}
			}

			if (node instanceof SequenceNode) {
				SequenceNode sn = (SequenceNode) node;
				for (Node subNode : sn.getChildren()) {
					if (subNode instanceof Fillable) {
						Fillable fillable = (Fillable) subNode;
						if (fillable.getSlot().getEnumList() != null) {
							for (String enu : fillable.getSlot().getEnumList()) {
								if (utterance.toLowerCase().contains(enu.toLowerCase())) {
									System.out.println("found" + enu + " in " + fillable.getName());
									Entity entity = new Entity(fillable.getName(), enu);
									res.add(entity);
								}
							}
						}
					}
				}
			}

		}

		return res;

	}

	public void print() {
		this.root.getAll().print();
	}

	public void invariant() {
		assert this.root != null : "no root node";
	}

	public JSONObject toBodyJSON() {

		return this.root.toBodyJSON();
	}

}
