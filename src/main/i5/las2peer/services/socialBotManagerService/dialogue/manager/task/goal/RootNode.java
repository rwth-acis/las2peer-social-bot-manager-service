package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import net.minidev.json.JSONObject;

public class RootNode extends Node {

    private Frame frame;
    private List<Node> children;

    public RootNode(Frame frame) {

	assert frame != null : "frame parameter is null";

	this.frame = frame;
	if (frame.getSlots() != null)
	    for (Slot childSlot : frame.getSlots().values()) {
		assert childSlot != null : "child slot is null";
		Node childNode = NodeFactory.create(childSlot);
		this.addChild(childNode);
	    }

	invariant();
    }

    @Override
    public boolean isReady() {
	for (Node node : this.getChildren())
	    if (!node.isReady())
		return false;
	return true;
    }

    @Override
    public boolean isFull() {
	for (Node node : this.getChildren())
	    if (!node.isFull())
		return false;
	return true;
    }

    @Override
    public boolean isConfirmed() {
	for (Node node : this.getChildren())
	    if (node.isConfirmed())
		return false;
	return true;
    }

    public void addChild(Node node) {
	assert node != null : "node is null";
	if (this.getChildren() == null)
	    this.setChildren(new ArrayList<Node>());
	this.getChildren().add(node);
    }

    public List<Node> getChildren() {
	return children;
    }

    public void setChildren(List<Node> children) {
	this.children = children;
    }

    public Frame getFrame() {
	return frame;
    }

    public void setFrame(Frame frame) {
	this.frame = frame;
    }

    @Override
    public Node next() {
	invariant();
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
    public void invariant() {
	assert this.frame != null : "frame is null";

    }

    @Override
    public Object toJSON() {
	invariant();

	JSONObject res = new JSONObject();
	for (Node node : this.children) {
	    res.merge(node.toJSON());
	}

	return res;
    }


}
