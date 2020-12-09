package i5.las2peer.services.socialBotManagerService.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import i5.las2peer.services.socialBotManagerService.model.BotConfiguration;
import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.model.BotModelAttribute;
import i5.las2peer.services.socialBotManagerService.model.BotModelEdge;
import i5.las2peer.services.socialBotManagerService.model.BotModelLabel;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;
import i5.las2peer.services.socialBotManagerService.model.BotModelNodeAttribute;
import i5.las2peer.services.socialBotManagerService.model.BotModelValue;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import i5.las2peer.services.socialBotManagerService.parser.creation.AccessServiceFunction;
import i5.las2peer.services.socialBotManagerService.parser.creation.Bot;
import i5.las2peer.services.socialBotManagerService.parser.creation.ChitChatFunction;
import i5.las2peer.services.socialBotManagerService.parser.creation.Function;
import i5.las2peer.services.socialBotManagerService.parser.creation.Message;
import i5.las2peer.services.socialBotManagerService.parser.creation.Messenger;
import i5.las2peer.services.socialBotManagerService.parser.creation.SlackMessenger;
import i5.las2peer.services.socialBotManagerService.parser.creation.TelegramMessenger;
import i5.las2peer.services.socialBotManagerService.parser.drawing.DrawingAlgorithm;
import i5.las2peer.services.socialBotManagerService.parser.drawing.SpringEmbedders;

public class BotModelParser {

	Map<BotModelNode, String> nodes = new LinkedHashMap<>();
	List<BotModelNode> messengers = new ArrayList<>();
	Map<String, BotModelNode> incomingMessages = new HashMap<>();

	LinkedHashMap<String, BotModelEdge> edgeList = new LinkedHashMap<>();
	LinkedHashMap<String, BotModelNode> nodeList = new LinkedHashMap<>();

	public String toJSON(BotModel botModel) {
		assert botModel != null;

		ObjectMapper mapper = new ObjectMapper();
		String jsonString = null;
		try {
			jsonString = mapper.writeValueAsString(botModel);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		System.out.println(jsonString);
		return jsonString;

	}

	public BotModel parse(Bot bot, BotConfiguration config) {

		BotModel model = new BotModel();
		model.setAttributes(addBotModelAttributes());

		// VLE Instance
		String vleName = "vleName";
		String vleAddress = "http://127.0.0.1:8070/";
		String seperator = "";
		BotModelNode vleNode = addNode("Instance");
		addAttribute(vleNode, "Name", vleName);
		addAttribute(vleNode, "Address", vleAddress);
		addAttribute(vleNode, "Environment Separator", seperator);

		// Bot
		BotModelNode botNode = addNode("Bot");
		assert bot.getName() != null : "bot has no name";
		addAttribute(botNode, "Name", bot.getName());
		addEdge(vleNode, botNode, "has");

		// NLU Knowledge
		String nluName = bot.getNluModule();
		String nluID = "0";
		String nluURL = "http://localhost:5005";

		Collection<LanguageUnderstander> nlus = config.getNlus().values();

		for (LanguageUnderstander nlu : nlus) {
			if (nlu.getName().contentEquals(nluName)) {
				System.out.println("found nlu module: " + nlu.getUrl());
				nluURL = nlu.getUrl();
			}
		}

		BotModelNode nluNode = addNode("NLU Knowledge");
		addAttribute(nluNode, "Name", nluName);
		addAttribute(nluNode, "ID", nluID);
		addAttribute(nluNode, "URL", nluURL);
		addEdge(botNode, nluNode, "has");

		// Messenger
		for (Messenger messenger : bot.getMessenger()) {
			BotModelNode messengerNode = addNode("Messenger");
			addEdge(botNode, messengerNode, "has");

			assert (messenger.getType() != null) : "messenger type is null";
			addAttribute(messengerNode, "Messenger Type", messenger.getType().toString());
			switch (messenger.getType()) {

			case TELEGRAM:
				TelegramMessenger tele = (TelegramMessenger) messenger;
				assert (tele.getToken() != null) : "no telegram authentication token";
				addAttribute(messengerNode, "Authentication Token", tele.getToken());
				addAttribute(messengerNode, "Name", "Telegram");
				break;

			case SLACK:
				SlackMessenger slack = (SlackMessenger) messenger;
				assert (slack.getToken() != null) : "no slack authentication token";
				addAttribute(messengerNode, "Authentication Token", slack.getToken());
				addAttribute(messengerNode, "Name", slack.getAppId());
				break;

			default:
				assert false : "no known messenger " + messenger.getType();
				break;
			}
		}

		// Function
		for (Function function : bot.getFunction()) {
			switch (function.getType()) {

			case CHIT_CHAT:
				ChitChatFunction fn = (ChitChatFunction) function;
				for (Message message : fn.getMessages()) {

					String intent = message.getIntent();
					if (incomingMessages.containsKey(intent)) {

						BotModelNode inNode = incomingMessages.get(intent);
						BotModelNode outNode = addNode("Chat Response");
					//	addAttribute(outNode, "Message", message.getResponse());
						addEdge(inNode, outNode, "triggers");

					} else {

						BotModelNode inNode = addNode("Incoming Message");
						addAttribute(inNode, "Intent Keyword", message.getIntent());
						addAttribute(inNode, "NLU ID", "0");

						BotModelNode outNode = addNode("Chat Response");
					//	addAttribute(outNode, "Message", message.getResponse());

						addEdge(inNode, outNode, "triggers");
						addMessengerEdges(inNode, "generates");

						incomingMessages.put(intent, inNode);
					}
				}
				break;

			case SERVICE_ACCESS:
				AccessServiceFunction as = (AccessServiceFunction) function;

				BotModelNode frameNode = addNode("Frame");
				//addAttribute(frameNode, "intent", as.getOperationID());

				BotModelNode actionNode = addNode("Bot Action");
				addAttribute(actionNode, "Action Type", "Service");
				//addAttribute(actionNode, "Function Name", as.getOperationID());
				//addAttribute(actionNode, "Service Alias", as.getServiceURL().toString());

				addEdge(frameNode, actionNode, "triggers");
				addMessengerEdges(frameNode, "generates");
				break;

			default:
				assert false : "no known function" + function.getType();
				break;

			}
		}

		processNodes();
		model.setEdges(edgeList);
		model.setNodes(nodeList);
		return model;

	}

	public BotModelNode addNode(String type) {

		BotModelNode node = new BotModelNode();
		node.setType(type);
		node.setAttributes(new LinkedHashMap<>());
		String id = getID();
		nodes.put(node, id);
		nodeList.put(id, node);

		node.setHeight(48);
		node.setWidth(48);
		node.setLabel(getLabel(id + "[label]", "Label", ""));

		if (node.getType().contentEquals("Messenger"))
			messengers.add(node);

		return node;
	}

	public void addAttribute(BotModelNode node, String name, String value) {

		if (node.getAttributes() == null)
			node.setAttributes(new LinkedHashMap<String, BotModelNodeAttribute>());

		String nodeId = nodes.get(node);
		LinkedHashMap<String, BotModelNodeAttribute> attributes = node.getAttributes();
		BotModelNodeAttribute attr = getNodeAttribute(nodeId, name, value);
		attributes.put(getID(), attr);
		node.setAttributes(attributes);

	}

	public BotModelAttribute addAttribute() {
		BotModelAttribute attribute = new BotModelAttribute();
		attribute.setType("ModelAttributesNode");
		attribute.setAttributes(new LinkedHashMap<>());
		attribute.setLabel(getLabel("modelAttributes[label]", "Label", "Model Attributes"));
		return attribute;
	}

	public BotModelNodeAttribute getNodeAttribute(String nodeId, String name, String value) {

		BotModelNodeAttribute res = new BotModelNodeAttribute();
		res.setName(name);
		nodeId = nodeId + "[" + name.toLowerCase() + "]";
		res.setId(nodeId);
		res.setValue(getValue(nodeId, name, value));
		return res;
	}

	public BotModelValue getValue(String id, String name, String value) {

		BotModelValue res = new BotModelValue();
		res.setId(id);
		res.setName(name);
		res.setValue(value);
		return res;
	}

	public BotModelLabel getLabel(String id, String name, String value) {
		BotModelLabel label = new BotModelLabel();
		label.setId(id);
		label.setName(name);
		label.setValue(getValue(id, name, value));

		return label;
	}

	public BotModelEdge addEdge(BotModelNode source, BotModelNode target, String type) {

		BotModelEdge edge = new BotModelEdge();
		edge.setSource(nodes.get(source));
		edge.setTarget(nodes.get(target));
		edge.setAttributes(new LinkedHashMap<>());
		edge.setType(type);
		String id = getID();
		edge.setLabel(getLabel(id + "[" + "label" + "]", "Label", ""));

		edgeList.put(id, edge);
		return edge;

	}

	public BotModelAttribute addBotModelAttributes() {

		BotModelAttribute res = new BotModelAttribute();
		res.setHeight(0);
		res.setWidth(0);
		res.setLeft(0);
		res.setTop(0);
		res.setzIndex(0);
		res.setType("ModelAttributesNode");
		res.setLabel(getLabel("modelAttributes[label]", "Label", "Model Attributes"));
		res.setAttributes(new LinkedHashMap<>());
		return res;

	}

	public void addMessengerEdges(BotModelNode target, String type) {

		for (BotModelNode messenger : this.messengers) {
			assert messenger.getType().contentEquals("Messenger") : "node should be a Messenger but is "
					+ messenger.getType();
			addEdge(messenger, target, type);
		}
	}

	public void processNodes() {

		for (BotModelNode node : this.nodeList.values()) {
			for (BotModelNodeAttribute attr : node.getAttributes().values()) {
				if (attr.getName().equalsIgnoreCase("Name")) {
					String id = this.nodes.get(node) + "[name]";
					String name = "Name";
					String value = attr.getValue().getValue();
					node.setLabel(getLabel(id, name, value));
				}
			}
		}

	}

	public String getID() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 24);
	}

	public BotModel order(BotModel model) {

		/*
		 * int z = 16001; int i = 4200; int j = 4200;
		 * 
		 * for (BotModelNode node : model.getNodes().values()) {
		 * 
		 * node.setzIndex(z); node.setLeft(i); node.setTop(j);
		 * 
		 * z++; i = i + 80; j = j + 80;
		 * 
		 * }
		 */

		DrawingAlgorithm algorithm = new SpringEmbedders(model);
		algorithm.execute();

		return model;
	}

}
