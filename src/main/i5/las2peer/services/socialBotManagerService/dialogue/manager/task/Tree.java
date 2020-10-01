package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import java.util.ArrayList;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class Tree extends Node {

	public Tree(Frame frame) {

		assert frame != null : "frame is null";

		this.frame = frame;
		if (frame.getSlots() != null)
			for (Slot childSlot : frame.getSlots().values()) {
				assert childSlot != null : "child slot is null";
				Node childNode = new Node(childSlot);
				this.addChild(childNode);
			}
	}

	public Tree(Slot slot) {
		super(slot);
	}

	private Frame frame;

	public Frame getFrame() {
		return frame;
	}

	public void setFrame(Frame frame) {
		this.frame = frame;
	}

	@Override
	public boolean isReady() {
		for (Node node : this.getChildren())
			if (node.isReady())
				return false;
		return true;
	}

	@Override
	public boolean isFull() {
		for (Node node : this.getChildren())
			if (node.isFull())
				return false;
		return true;
	}

	@Override
	public void addChild(Node node) {
		assert node != null : "node is null";
		if (this.getChildren() == null)
			this.setChildren(new ArrayList<Node>());
		this.getChildren().add(node);
	}

}
