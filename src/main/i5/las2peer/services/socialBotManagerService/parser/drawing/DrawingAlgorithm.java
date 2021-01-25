package i5.las2peer.services.socialBotManagerService.parser.drawing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.model.BotModelEdge;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;

public abstract class DrawingAlgorithm {

	BotModel model;

	public DrawingAlgorithm(BotModel model) {
		this.model = model;
	}

	public abstract void execute();

	protected double euclidean(BotModelNode n1, BotModelNode n2) {

		double x1 = n1.getLeft();
		double y1 = n1.getTop();
		double x2 = n2.getLeft();
		double y2 = n2.getTop();

		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}

	protected Vector position(BotModelNode node) {
		return new Vector(node.getLeft(), node.getTop());
	}

	protected Collection<String> adjacent(String nodeID) {

		Collection<String> res = new HashSet<>();
		Map<String, BotModelEdge> edges = model.getEdges();

		for (BotModelEdge edge : edges.values()) {
			if (edge.getSource().contentEquals(nodeID))
				res.add(edge.getTarget());
			else if (edge.getTarget().contentEquals(nodeID))
				res.add(edge.getSource());
		}
		return res;
	}

	protected Collection<String> nonAdjacent(Collection<String> nodeIDs) {

		Collection<String> res = new HashSet<>();
		Map<String, BotModelNode> nodes = model.getNodes();

		for (String nodeID : nodes.keySet()) {
			if (!nodeIDs.contains(nodeID))
				res.add(nodeID);
		}
		return res;
	}

	protected static Map<String, BotModelNode> randomOrder(Map<String, BotModelNode> nodes, int x, int y) {
		
		int size = nodes.size() * 100;
		for (BotModelNode node : nodes.values()) {
			node.setHeight(48);
			node.setWidth(48);
			node.setLeft((int) (Math.random() * size) + x);
			node.setTop((int) (Math.random() * size) + y);
		}
		
		return nodes;
		
	}
	
	protected void randomOrder(int x, int y) {
				
		Map<String, BotModelNode> nodes = model.getNodes();

		int size = nodes.size() * 100;
		for (BotModelNode node : nodes.values()) {
			node.setHeight(48);
			node.setWidth(48);
			node.setLeft((int) (Math.random() * size) + x);
			node.setTop((int) (Math.random() * size) + y);
		}

	}

}
