package i5.las2peer.services.socialBotManagerService.parser.drawing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.model.BotModelEdge;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;

public class DrawingAlgorithmTest {

    BotModel model;
    LinkedHashMap<String, BotModelNode> nodes = new LinkedHashMap<>();
    LinkedHashMap<String, BotModelEdge> edges = new LinkedHashMap<>();

    BotModelNode n1 = new BotModelNode();
    BotModelNode n2 = new BotModelNode();
    BotModelNode n3 = new BotModelNode();
    String n1ID = "n1";
    String n2ID = "n2";
    String n3ID = "n3";

    BotModelEdge e12 = new BotModelEdge();
    BotModelEdge e13 = new BotModelEdge();
    String e12ID = "e12";
    String e13ID = "e13";

    @Before
    public void setUp() {

	model = new BotModel();

	nodes = new LinkedHashMap<>();
	nodes.put(n1ID, n1);
	nodes.put(n2ID, n2);
	nodes.put(n3ID, n3);

	e12.setSource(n1ID);
	e12.setTarget(n2ID);
	e13.setSource(n1ID);
	e13.setTarget(n3ID);

	edges = new LinkedHashMap<>();
	edges.put(e12ID, e12);
	edges.put(e13ID, e13);

	model.setEdges(edges);
	model.setNodes(nodes);

    }

    @Test
    public void randomOrderTest() {

	DrawingAlgorithm algorithm = new SpringEmbedders(model);
	algorithm.randomOrder(4000, 4000);
	for (BotModelNode node : nodes.values()) {
	    System.out.println("x: " + node.getLeft() + ", y: " + node.getTop());
	    assertTrue(node.getLeft() > 0);
	    assertTrue(node.getTop() > 0);
	}

    }


    @Test
    public void adjacentTest() {
	DrawingAlgorithm algorithm = new SpringEmbedders(model);

	Collection<String> adj = algorithm.adjacent(n1ID);
	assertTrue(adj.contains(n2ID));
	assertTrue(adj.contains(n3ID));

	adj = algorithm.adjacent(n2ID);
	assertTrue(adj.contains(n1ID));
	assertFalse(adj.contains(n3ID));

	adj = algorithm.adjacent(n3ID);
	assertTrue(adj.contains(n1ID));
	assertFalse(adj.contains(n2ID));

    }


    @Test
    public void nonAdjacentTest() {

	DrawingAlgorithm algorithm = new SpringEmbedders(model);
	Collection<String> adj = algorithm.nonAdjacent(algorithm.adjacent(n1ID));
	assertFalse(adj.contains(n2ID));
	assertFalse(adj.contains(n3ID));

	adj = algorithm.nonAdjacent(algorithm.adjacent(n2ID));
	assertFalse(adj.contains(n1ID));
	assertTrue(adj.contains(n3ID));

    }

}
