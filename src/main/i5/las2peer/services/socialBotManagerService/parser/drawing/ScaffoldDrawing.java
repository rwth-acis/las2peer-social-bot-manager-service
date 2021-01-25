package i5.las2peer.services.socialBotManagerService.parser.drawing;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;

public class ScaffoldDrawing extends DrawingAlgorithm {

	Random random;

	public ScaffoldDrawing(BotModel model) {
		super(model);
		random = new Random();
	}

	@Override
	public void execute() {

		BotModelNode botNode = null;
		BotModelNode vleNode = null;
		Map<String, BotModelNode> knowledges = new HashMap<>();
		Map<String, BotModelNode> messengers = new HashMap<>();
		Map<String, BotModelNode> nodes = new HashMap<>();

		for (Entry<String, BotModelNode> entry : model.getNodes().entrySet()) {
			BotModelNode node = entry.getValue();
			String key = entry.getKey();
			switch (node.getType()) {
			case "Instance":
				vleNode = node;
				break;
			case "Bot":
				botNode = node;
				break;
			case "Messenger":
				messengers.put(key, node);
				break;
			case "Knowledge":
				knowledges.put(key, node);
				break;
			default:
				nodes.put(key, node);
				break;
			}
		}
		
		botNode.setLeft(4400);
		botNode.setTop(4320);
		vleNode.setLeft(4400);
		vleNode.setTop(4160);
		
		// knowledge nodes
		int left = 4250;
		int top = 4300 + randInt(0, 30);
		int i = 0;
		for(BotModelNode node :knowledges.values()) {
			node.setLeft(left + randInt(0, 10));
			node.setTop(top + i * 200 + randInt(0, 10));
		}
		
		// messenger nodes
		left = 4565 + randInt(0, 10);
		top = 4300 + randInt(0, 30);
		i = 0;
		for(BotModelNode node :messengers.values()) {
			node.setLeft(left + randInt(0, 10));
			node.setTop(top + i * 200 + randInt(0, 10));		
		}
		
		// dialogue nodes
		nodes = DrawingAlgorithm.randomOrder(nodes, 4700, 4200);
		SpringEmbedders se = new SpringEmbedders(null);
		se.springEmbed(nodes, 30);
		
	}

	public int randInt(int min, int max) {
		int randomNum = random.nextInt((max - min) + 1) + min;
		return randomNum;
	}

}
