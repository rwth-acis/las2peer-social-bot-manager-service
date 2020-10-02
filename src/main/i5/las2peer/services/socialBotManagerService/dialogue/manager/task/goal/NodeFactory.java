package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class NodeFactory {

    public static Node create(Frame frame) {
	return new RootNode(frame);
    }

    public static Node create(Slot slot) {

	assert slot != null : "slot is null";

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
	    return new SequenceNode(slot);
	}

	// Repetition Node
	if (slot.hasChildren() && slot.isArray()) {
	    return new SequenceNode(slot);
	}

	assert false : " slot cant be assigned to a node ";
	return null;
    }

}
