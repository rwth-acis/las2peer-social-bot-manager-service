package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ResponseParseMode;

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

		node.fill(value);

		return true;
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
		if (getFrame().getDescendants() == null)
			return false;
		return getFrame().getDescendants().contains(name);
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
		System.out.println("reset dialogue goal");
	}

	public Frame getFrame() {
		return this.root.getFrame();
	}

	public OpenAPIAction getOpenAPIAction() {

		OpenAPIAction res = new OpenAPIAction();
		ServiceFunction function = this.getFrame().getServiceFunction();
		res.setFunction(function);
		res.setBodyParameter(root.toJSON());
		res.setPathParameters(root.getPathParameters());
		res.setQueryParameters(root.getQueryParameters());

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
		
		if(this.root.getChildren() == null)
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

	public RootNode getRoot() {
		return this.root;
	}

	public void invariant() {
		assert this.root != null : "no root node";
	}

	public Object toJSON() {

		return this.getRoot().toJSON();
	}

}
