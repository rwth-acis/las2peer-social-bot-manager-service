package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import net.minidev.json.JSONObject;

public class WrapperNode extends SequenceNode {

	private Node wrapper;

	public WrapperNode() {
		super();
	}

	public void setWrapperNode(Node node) {
		assert node != null;
		assert getChildren().contains(node) : "wrapper node not contained in children";

		System.out.println("wrapper set to " + node.toString());
		this.wrapper = node;
	}

	@Override
	public JSONObject toBodyJSON() {
		invariant();

		JSONObject wrapperJson = this.wrapper.toBodyJSON();
		for (Node node : getChildren()) {
			if (node != this.wrapper) {
				JSONObject nodeJson = node.toBodyJSON();
				if (nodeJson != null && !nodeJson.isEmpty())
					wrapperJson.putAll(node.toBodyJSON());
			}
		}

		return wrapperJson;
	}
	
	@Override
	public String getAPIName() {
		return this.wrapper.getAPIName() + "wrapper";
	}
	
	@Override
	public String getDisplayName() {
		return this.wrapper.getDisplayName() + "wrapper";
	}
}
