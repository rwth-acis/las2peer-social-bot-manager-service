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
		if (slot.isLeaf() && !slot.isArray() && slot.getParameter() != null) {
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

			System.out.println("create Selection node " + slot.getAPIName() + ":");
			for (Slot sb : slot.getChildren()) {
				System.out.println(sb.getAPIName() + " " + sb.getPriority());
			}

			if (slot.getChildren(-1).isEmpty()) {
				System.out.println("create simple selection node " + slot.getName());
				return res;
			}

			System.out.println("create complicated selection node " + slot.getName());
			WrapperNode node = new WrapperNode();
			for (Slot wslot : slot.getChildren(-1)) {
				node.addChild(create(wslot));
				System.out.println("special child " + wslot.getName());
			}

			node.addChild(res);
			node.setWrapperNode(res);
			return node;

		}

		// Repetition Node
		if (slot.hasChildren() && slot.isArray()) {
			return new RepetitionNode(slot);
		}

		System.out.println("slot " + slot.getName() + "cant be assigned to a regular node: create empty");
		return new SequenceNode();
	}

	public static Node createIgnoreArray(Slot slot) {

		assert slot != null : "slot parameter is null";
		assert slot.hasChildren() : "slot has no children";

		// Selection Node
		if (slot.isSelection())
			return new SelectionNode(slot);

		// Sequence Node
		if (!slot.isSelection())
			return new CombinerNode(slot);

		return null;
	}

}
