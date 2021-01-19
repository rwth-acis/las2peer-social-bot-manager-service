package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class NodeFactory {

	public static Node create(Frame frame) {
		return new RootNode(frame);
	}

	public static Node create(Slot slot) {

		assert slot != null : "slot is null";
		assert !(slot.isLeaf() && slot.isSelection()) : "selection slot must have children";
		System.out.println("node: " + slot.getName());

		// Value Node
		if (slot.isLeaf() && !slot.isArray()) {
			return new ValueNode(slot);
		}

		// Multiple Values Node
		if (slot.isLeaf() && slot.isArray()) {
			return new MultiValueNode(slot);
		}

		// Sequence Node
		if (slot.hasChildren() && !slot.isSelection() && !slot.isArray()) {
			return new SequenceNode(slot);
		}

		// Selection Node
		if (slot.hasChildren() && slot.isSelection() && !slot.isArray()) {
			SelectionNode res = new SelectionNode(slot);
			if(slot.getChildren(-1).isEmpty())
				return res;
			
			SequenceNode node = new SequenceNode();			
			for(Slot wslot :slot.getChildren(-1)) 
				node.addChild(create(wslot));
			node.addChild(res);
			node.setWrapperNode(res);
			
		}

		// Repetition Node
		if (slot.hasChildren() && slot.isArray()) {
			return new RepetitionNode(slot);
		}

		assert false : " slot cant be assigned to a node ";
		return null;
	}

	public static Node createIgnoreArray(Slot slot) {

		assert slot != null : "slot parameter is null";
		assert slot.hasChildren() : "slot has no children";

		// Selection Node
		if (slot.isSelection()) {
			return new SelectionNode(slot);
		}

		// Sequence Node
		if (!slot.isSelection()) {
			return new SequenceNode(slot);
		}

		return null;
	}

}
