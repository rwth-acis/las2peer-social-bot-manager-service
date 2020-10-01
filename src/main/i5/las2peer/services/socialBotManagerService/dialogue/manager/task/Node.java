package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class Node {

	private Slot slot;
	private String value;
	private boolean confirmed;
	private List<Node> children;

	public Node() {

	}

	public Node(Slot slot) {

		assert slot != null : "slot is null";

		this.slot = slot;
		this.value = null;
		this.confirmed = false;
		this.children = new ArrayList<Node>();
		for (Slot childSlot : slot.getChildren()) {
			Node childNode = new Node(childSlot);
			this.children.add(childNode);
		}

		invariant();
	}

	public void fill(String value) {

		assert value != null : "parameter value is null";
		invariant();
		assert this.slot.validate(value) : "value '" + value + "' is not valid for slot " + this.slot.getName();

		this.value = value;

	}

	public void confirm(String value) {

		assert this.value != null : "dialogue goal node: confirm: this.value is null";
		assert value != null : "dialogue goal node: confirm: value is null";
		assert this.value.contentEquals(value) : "dialogue goal node: value confirmed that was not filled yet";
		invariant();

		this.confirmed = true;

	}

	public void clear() {

		invariant();

		this.value = null;
		this.confirmed = false;

		for (Node node : this.children)
			node.clear();

	}

	public boolean isFilled() {
		invariant();

		return this.value != null && !this.value.contentEquals("");
	}

	public boolean isConfirmed() {
		invariant();

		return confirmed;
	}

	public boolean isReady() {
		invariant();

		// leaf node
		if (this.slot.isLeaf()) {
			if (this.isFilled())
				return true;
			if (!this.slot.isRequired())
				return true;
			return false;
		}

		// selection (one child is ready)
		if (this.slot.isSelection())
			if (!this.isFilled())
				return false;
			else {
				for (Node node : this.children)
					if (node.isReady())
						return true;
				return false;
			}

		// sequence (all children are ready)
		if (this.slot.hasChildren()) {
			for (Node node : this.children)
				if (node.isReady())
					return false;
			return true;
		}

		assert false : "should not be reached";
		return false;

	}

	public boolean isFull() {
		invariant();

		// leaf node
		if (this.slot.isLeaf()) {
			if (this.isFilled())
				return true;
			if (!this.slot.isRequired())
				return true;
			return false;
		}

		// selection (one child is ready)
		if (this.slot.isSelection())
			if (!this.isFilled())
				return false;
			else {
				for (Node node : this.children)
					if (node.isFull())
						return true;
				return false;
			}

		// sequence (all children are ready)
		if (this.slot.hasChildren()) {
			for (Node node : this.children)
				if (node.isFull())
					return false;
			return true;
		}

		assert false : "should not be reached";
		return false;

	}

	public Collection<Node> getDescendants() {

		assert this.getChildren() != null : "children are null";

		Collection<Node> desc = new ArrayList<Node>();
		if (!(this instanceof Tree))
			desc.add(this);
		for (Node node : this.getChildren())
			desc.addAll(node.getDescendants());

		return desc;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Slot getSlot() {
		return slot;
	}

	public void setSlot(Slot slot) {
		this.slot = slot;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public void addChild(Node node) {

		invariant();

		this.children.add(node);
	}

	public void invariant() {

		if (!(this instanceof Tree))
			assert this.slot != null : "dialogue goal node has no slot";
		if (this.value != null)
			assert this.slot.validate(this.value) : "slot " + this.slot.getName() + "is filled with invalid value '"
			+ this.value + "'";

	}

}
