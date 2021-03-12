package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import i5.las2peer.services.socialBotManagerService.model.Slot;
import net.minidev.json.JSONObject;

public class CombinerNode extends SequenceNode {

	String name;

	public CombinerNode(Slot slot) {

		super();
		assert slot != null : "slot parameter is null";
		assert slot.getChildren() != null : "slot has no children";

		for (Slot childSlot : slot.getChildren()) {
			Node node = NodeFactory.create(childSlot);
			addChild(node);
		}

		this.name = slot.getAPIName() + "_combiner";
	}

	@Override
	public JSONObject toBodyJSON() {

		JSONObject res = new JSONObject();
		for (Node node : getChildren()) {
			JSONObject nodeJson = node.toBodyJSON();
			if (nodeJson != null && !nodeJson.isEmpty())
				res.putAll(node.toBodyJSON());

		}

		return res;
	}

	@Override
	public String getDisplayName() {
		return this.name;
	}

}
