package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.model.Slot;
import net.minidev.json.JSONObject;

public class SelectionNode extends Node implements Fillable {

	private Slot slot;
	private String value;
	private boolean confirmed;
	private Map<String, Node> children;

	public SelectionNode(Slot slot) {

		assert slot != null : "slot parameter is null";
		assert slot.isSelection() : "slot is no selection";

		this.slot = slot;
		this.value = null;
		this.confirmed = false;
		this.children = new HashMap<String, Node>();
		for (Slot childSlot : slot.getChildren()) {
			Node childNode = NodeFactory.create(childSlot);
			this.children.put(childSlot.getEntity(), childNode);
		}

		invariant();
	}

	@Override
	public void fill(String value) {

		assert value != null : "value parameter is null";
		invariant();
		assert this.slot.validate(value) : "value '" + value + "' is not valid for slot " + this.slot.getName();

		this.value = value;

	}

	@Override
	public boolean validate(String value) {

		assert value != null : "value parameter is null";
		invariant();

		return this.slot.validate(value);
	}

	@Override
	public void confirm() {

		invariant();
		assert this.isFilled() : "node value not filled yet";
		assert this.value.contentEquals(value) : "value confirmed that was not filled yet";

		this.confirmed = true;

	}

	@Override
	public void clear() {
		invariant();

		this.value = null;
		this.confirmed = false;

	}

	@Override
	public boolean isFilled() {
		invariant();
		return this.value != null && !(this.value.contentEquals(""));
	}

	@Override
	public boolean isReady() {
		invariant();
		if (!this.isFilled())
			return false;

		Node node = this.children.get(this.value);
		return node.isReady();
	}

	@Override
	public boolean isFull() {
		invariant();
		if (!this.isFilled())
			return false;

		Node node = this.children.get(this.value);
		return node.isFull();
	}

	@Override
	public boolean isConfirmed() {
		invariant();
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	@Override
	public Slot getSlot() {
		return slot;
	}

	public void setSlot(Slot slot) {
		this.slot = slot;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Map<String, Node> getChildren() {
		return children;
	}

	public void setChildren(Map<String, Node> children) {
		this.children = children;
	}

	public Node getChild(String enu) {
		return this.children.get(enu);
	}

	public Node getActive() {
		return this.children.get(this.value);
	}

	@Override
	public Node next() {
		invariant();
		if (!this.isFilled())
			return this;

		return this.children.get(this.value).next();
	}

	@Override
	public NodeList getAll() {
		NodeList nodes = new NodeList(this);
		if (this.isFilled())
			nodes.addAll(getChild(this.value).getAll());
		return nodes;
	}

	@Override
	public void invariant() {
		assert this.slot != null : "slot of value node is null";
		assert this.children != null : "children map is null";
		assert !this.children.isEmpty() : "selection node has no children";
		if (this.value != null) {
			assert this.slot.validate(this.getValue()) : "slot " + this.slot.getName() + " filled with invalid value "
					+ this.value;
			assert this.children.containsKey(this.value) : "no child path with value " + this.value;
		}
	}

	@Override
	public JSONObject toJSON() {
		invariant();

		JSONObject res = new JSONObject();
		res.put(this.getAPIName(), this.getValue());
		if (this.isFilled())
			res.merge(this.getActive().toJSON());

		return res;
	}

}
