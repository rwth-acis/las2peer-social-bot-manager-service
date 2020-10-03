package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;

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
    public boolean isFilled() {
	invariant();
	for (Node node : this.children) {
	    if (!node.isFilled()) {
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
	return "SequenceNode [children=" + children + ", isFilled()=" + isFilled() + ", isReady()=" + isReady()
		+ ", isConfirmed()=" + isConfirmed() + "]";
    }

}
