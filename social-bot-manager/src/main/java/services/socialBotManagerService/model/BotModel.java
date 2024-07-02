package services.socialBotManagerService.model;

import java.io.Serializable;
import java.util.LinkedHashMap;


public class BotModel implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2889244909583713463L;
	/**
	 *
	 */
	private BotModelAttribute attributes;
	private LinkedHashMap<String, BotModelNode> nodes;
	private LinkedHashMap<String, BotModelEdge> edges;
	public BotModelAttribute getAttributes() {
		return attributes;
	}
	public void setAttributes(BotModelAttribute attributes) {
		this.attributes = attributes;
	}
	public LinkedHashMap<String, BotModelNode> getNodes() {
		return nodes;
	}
	public void setNodes(LinkedHashMap<String, BotModelNode> nodes) {
		this.nodes = nodes;
	}
	public LinkedHashMap<String, BotModelEdge> getEdges() {
		return edges;
	}
	public void setEdges(LinkedHashMap<String, BotModelEdge> edges) {
		this.edges = edges;
	}
}
