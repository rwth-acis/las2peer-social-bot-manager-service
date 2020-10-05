package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;
import net.minidev.json.JSONObject;

public class SequenceNode extends Node {

    private List<Node> children;

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

    @Override
    public boolean isFull() {
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
	for(Node node :this.children) {
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
    public void invariant() {
	assert this.children != null : "children are null";
	assert !this.children.isEmpty() : "sequence node has no children";
    }

    @Override
    public String toString() {
	return "SequenceNode [children=" + children + ", isFull()=" + isFull() + ", isReady()=" + isReady()
		+ ", isConfirmed()=" + isConfirmed() + "]";
    }

    @Override
    public JSONObject toJSON() {
	// TODO Auto-generated method stub
	return null;
    }

}
