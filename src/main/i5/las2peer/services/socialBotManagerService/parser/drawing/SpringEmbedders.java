package i5.las2peer.services.socialBotManagerService.parser.drawing;

import java.util.Map;

import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.model.BotModelEdge;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;

public class SpringEmbedders extends DrawingAlgorithm {

    int iterations = 200;
    double crep = 1;
    double cspring = 1;
    double threshold = 0.1;
    double idealLength = 60;
    double scaling = 1;

    public SpringEmbedders(BotModel model) {
	super(model);
    }

    @Override
    public void execute() {

	Map<String, BotModelNode> nodes = model.getNodes();
	Map<String, BotModelEdge> edges = model.getEdges();

	randomOrder();
	springEmbed(nodes, edges, iterations);

    }

    public void springEmbed(Map<String, BotModelNode> nodes, Map<String, BotModelEdge> edges, int iterations) {

	int t = 0;
	double max = 0;

	while (t < iterations && max > threshold)

	    for (String nodeID : nodes.keySet()) {

		Vector F = displacement(nodeID);

		double f = F.norm();
		if (f > max)
		    max = f;

		BotModelNode node = nodes.get(nodeID);
		F.scalar(scaling);
		node.setLeft(node.getLeft() + F.x);
		node.setTop((node.getTop() + F.y));
		t++;
	    }
    }

    protected Vector repulsiveForce(BotModelNode n1, BotModelNode n2) {

	Vector res = new Vector(n1, n2);
	res.unit();
	double s = crep / Math.pow(euclidean(n1, n2), 2);
	res.scalar(s);
	return res;
    }

    protected Vector attractiveForce(BotModelNode n1, BotModelNode n2) {

	Vector res = new Vector(n1, n2);
	res.unit();
	double s = cspring * Math.log(euclidean(n1, n2) / idealLength);
	res.scalar(s);
	return res;
    }

    protected Vector displacement(String nodeID) {

	Map<String, BotModelNode> nodes = model.getNodes();
	Map<String, BotModelEdge> edges = model.getEdges();

	Vector F = new Vector(0, 0);
	for (String adjacentID : adjacent(nodeID))
	    F.add(attractiveForce(nodes.get(nodeID), nodes.get(adjacentID)));
	for (String nonAdjacentID : nonAdjacent(adjacent(nodeID)))
	    F.add(repulsiveForce(nodes.get(nodeID), nodes.get(nonAdjacentID)));

	return F;

    }

}
