package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;
import net.minidev.json.JSONObject;

public class SequenceNode extends Node {

	private List<Node> children;
	private Node wrapper;
	
	public SequenceNode(Slot slot) {

		assert slot != null : "slot parameter is null";
		assert slot.getChildren() != null : "slot has no children";

		this.children = new ArrayList<Node>();
		for (Slot childSlot : slot.getChildren()) {
			Node node = NodeFactory.create(childSlot);
			this.children.add(node);
		}

		invariant();
	}

	public SequenceNode() {
		this.children = new ArrayList<Node>();
	}

	public void setWrapperNode(Node node) {
		assert node != null;
		assert this.children.contains(node) : "wrapper node not contained in children";

		System.out.println("wrapper set to " + node.toString());
		this.wrapper = node;
	}

	@Override
	public boolean isFull() {
		
		if(this.children.isEmpty())
			return true;
		
		invariant();
		for (Node node : this.children) {
			if (!node.isFull()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isReady() {
		invariant();
		
		if(this.children.isEmpty())
			return true;
		
		for (Node node : this.children) {
			if (!node.isReady()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isConfirmed() {
		invariant();
		
		if(this.children.isEmpty())
			return true;
		
		for (Node node : this.children) {
			if (!node.isConfirmed()) {
				return false;
			}
		}
		return true;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void addChild(Node node) {
		assert node != null : "node parameter is null";
		invariant();

		this.children.add(node);
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	@Override
	public Node next() {
		invariant();

		System.out.println("SequenceNode next: " + this.children.size());
		
		if(this.children.isEmpty())
			return null;
		
		for (Node node : this.children) {			
			if (node instanceof Slotable && ((Slotable) node).getSlot() != null) {				
				Slot slot = ((Slotable) node).getSlot();
				System.out.println(slot.getName() + " " + slot.getPriority());
				if (!node.isReady() && slot.getPriority() == -1)
					return node.next();
			}
		}

		for (Node node : this.children) {
			if (!node.isReady()) {
				return node.next();
			}
		}

		for (Node node : this.children) {
			if (!node.isFull()) {
				return node.next();
			}
		}

		return null;
	}

	@Override
	public NodeList getAll() {
		NodeList nodes = new NodeList(this);
		for (Node node : this.children) {
			nodes.addAll(node.getAll());
		}
		return nodes;
	}

	@Override
	protected void invariant() {
		assert this.children != null : "children are null";

	}

	@Override
	public String toString() {
		return "SequenceNode [children=" + children + ", isFull()=" + isFull() + ", isReady()=" + isReady()
				+ ", isConfirmed()=" + isConfirmed() + "]";
	}

	@Override
	public JSONObject toBodyJSON() {
		invariant();

		if (this.wrapper == null) {

			// normal sequence
			JSONObject res = new JSONObject();
			for (Node node : this.children) {
				JSONObject nodeJson = node.toBodyJSON();
				if (nodeJson != null && !nodeJson.isEmpty())
					res.putAll(node.toBodyJSON());
			}
			return res;

		} else {

			// combiner
			JSONObject wrapperJson = this.wrapper.toBodyJSON();
			for (Node node : this.children) {
				if (node != this.wrapper) {
					JSONObject nodeJson = node.toBodyJSON();
					if (nodeJson != null && !nodeJson.isEmpty())
						wrapperJson.putAll(node.toBodyJSON());
				}
			}

			return wrapperJson;
		}

	}

}
