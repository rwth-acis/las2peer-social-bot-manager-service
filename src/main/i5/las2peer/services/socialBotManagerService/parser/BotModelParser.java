package i5.las2peer.services.socialBotManagerService.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import i5.las2peer.services.socialBotManagerService.parser.creation.Bot;
import i5.las2peer.services.socialBotManagerService.parser.creation.ChitChatFunction;
import i5.las2peer.services.socialBotManagerService.parser.creation.Message;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.AccessServiceFunction;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.Function;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.Las2peer;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.OpenAPI;
import i5.las2peer.services.socialBotManagerService.parser.creation.messenger.Messenger;
import i5.las2peer.services.socialBotManagerService.parser.creation.messenger.SlackMessenger;
import i5.las2peer.services.socialBotManagerService.parser.creation.messenger.TelegramMessenger;
import i5.las2peer.services.socialBotManagerService.parser.drawing.DrawingAlgorithm;
import i5.las2peer.services.socialBotManagerService.parser.drawing.SpringEmbedders;
import i5.las2peer.services.socialBotManagerService.parser.nlg.NLGDataGroup;
import i5.las2peer.services.socialBotManagerService.parser.nlg.NLGTrainingData;

public class BotModelParser {

	BotModelInfo info = new BotModelInfo();
	Map<BotModelNode, String> nodes = new LinkedHashMap<>();
	List<BotModelNode> messengers = new ArrayList<>();
	Map<String, BotModelNode> incomingMessages = new HashMap<>();

	LinkedHashMap<String, BotModelEdge> edgeList = new LinkedHashMap<>();
	LinkedHashMap<String, BotModelNode> nodeList = new LinkedHashMap<>();

	BotConfiguration config;

	public BotModelParser(BotConfiguration config) {
		this.config = config;
	}

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

	public BotModel parse(BotModel model, Function function) {

		this.nodeList = model.getNodes();
		this.edgeList = model.getEdges();
		for (BotModelNode node : model.getNodes().values()) {
			if (node.getType().contentEquals("Messenger")) {
				this.messengers.add(node);
			}
		}
		
		for (Entry<String, BotModelNode> entry : model.getNodes().entrySet()) {
			BotModelNode node = entry.getValue();
			if (node.getType().contentEquals("Incoming Message")) {
				for(BotModelNodeAttribute attr :node.getAttributes().values())
					if(attr.getName().contentEquals("Intent")) {
						this.incomingMessages.put(attr.getValue().getValue(), node);
						System.out.println("add existing incoming Message for " + attr.getValue().getValue());
					}
			}
		}

		// Function
		System.out.println("function type " + function.getType());
		switch (function.getType()) {

		case CHIT_CHAT:
			ChitChatFunction fn = (ChitChatFunction) function;
			for (Message message : fn.getMessages()) {
				try {
					String intent = message.getIntent();
					if (incomingMessages.containsKey(intent)) {

						BotModelNode inNode = incomingMessages.get(intent);
						BotModelNode outNode = addNode("Chat Response");
						addAttribute(outNode, "Message", message.getResponse());
						addEdge(inNode, outNode, "triggers");

					} else {

						BotModelNode inNode = addNode("Incoming Message");
						addAttribute(inNode, "Intent Keyword", message.getIntent());
						addAttribute(inNode, "NLU ID", "0");

						BotModelNode outNode = addNode("Chat Response");
						addAttribute(outNode, "Message", message.getResponse());

						addEdge(inNode, outNode, "triggers");
						addMessengerEdges(inNode, "generates");

						incomingMessages.put(intent, inNode);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;

		case SERVICE_ACCESS:
			AccessServiceFunction as = (AccessServiceFunction) function;

			if (as.getServiceType() instanceof OpenAPI) {
				try {
					OpenAPI oa = (OpenAPI) as.getServiceType();
					BotModelNode frameNode = addNode("Frame");
					addAttribute(frameNode, "Intent Keyword", oa.getIntent());

					BotModelNode actionNode = addNode("Bot Action");
					addAttribute(actionNode, "Action Type", "OpenAPI");
					addAttribute(actionNode, "FunctionName", oa.getFunctionName());
					addAttribute(actionNode, "Service Alias", oa.getBaseURL().toString());

					addEdge(frameNode, actionNode, "triggers");
					addMessengerEdges(frameNode, "generates");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (as.getServiceType() instanceof Las2peer) {

				try {
					Las2peer oa = (Las2peer) as.getServiceType();
					BotModelNode frameNode = addNode("Frame");
					addAttribute(frameNode, "Intent Keyword", oa.getIntent());

					BotModelNode actionNode = addNode("Bot Action");
					addAttribute(actionNode, "Action Type", "Service");
					addAttribute(actionNode, "FunctionName", oa.getFunctionName());
					addAttribute(actionNode, "Service Alias", oa.getServiceAlias());

					addEdge(frameNode, actionNode, "triggers");
					addMessengerEdges(frameNode, "generates");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			break;
		default:
			assert false : "no known function" + function.getType();
			break;

		}

		BotModel newBotModel = new BotModel();
		processNodes();
		newBotModel.setEdges(this.edgeList);
		newBotModel.setNodes(this.nodeList);

		return newBotModel;
	}

	public BotModel parse(BotModel model, NLGTrainingData data) {

		this.nodeList = model.getNodes();
		this.edgeList = model.getEdges();
		for (BotModelNode node : model.getNodes().values()) {
			if (node.getType().contentEquals("Messenger")) {
				this.messengers.add(node);
			}
		}

		// search Knowledge Node
		BotModelNode nlgNode = null;
		for (BotModelNode node : model.getNodes().values()) {
			if (node.getType().contentEquals("Knowledge")) {
				for (BotModelNodeAttribute attr : node.getAttributes().values()) {
					if (attr.getName().contentEquals("Type") && attr.getValue().getValue().contentEquals("Generation"))
						nlgNode = node;
				}
			}
		}

		// add Knowledge Node
		if (nlgNode == null) {
			nlgNode = this.addNode("Knowledge");
			this.addAttribute(nlgNode, "Name", "abcNLG");

			for (BotModelNode node1 : model.getNodes().values()) {
				if (node1.getType().contentEquals("Bot"))
					this.addEdge(node1, nlgNode, "has");
			}
		}

		// add messages
		for (NLGDataGroup d : data.getDatagroups()) {
			BotModelNode nn = this.addNode("Incoming Message");
			this.addAttribute(nn, "Intent Keyword", d.getIntent());
			this.addAttribute(nn, "Response Message", d.getResponse());
			this.addEdge(nlgNode, nn, "has");
		}

		BotModel newBotModel = new BotModel();
		processNodes();
		newBotModel.setEdges(this.edgeList);
		newBotModel.setNodes(this.nodeList);

		return newBotModel;
	}

	public BotModel parse(Bot bot, BotConfiguration config) {

		if (config.getBotModelInfo() != null)
			this.info = config.getBotModelInfo();

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
		addAttribute(botNode, "Language", "English");
		addAttribute(botNode, "Name", bot.getName());
		addAttribute(botNode, "Description", bot.getDescription());
		addEdge(vleNode, botNode, "has");

		// NLU Knowledge
		LanguageUnderstander nluu = config.getNLU(bot.getNluModule());
		assert nluu != null : "no nlu module found for " + bot.getNluModule();
		String nluName = bot.getNluModule();
		String nluID = "0";
		String nluURL = nluu.getUrl();

		BotModelNode nluNode = addNode("Knowledge");
		addAttribute(nluNode, "Type", "Understanding");
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
						addAttribute(outNode, "Message", message.getResponse());
						addEdge(inNode, outNode, "triggers");

					} else {

						BotModelNode inNode = addNode("Incoming Message");
						addAttribute(inNode, "Intent Keyword", message.getIntent());
						addAttribute(inNode, "NLU ID", "0");

						BotModelNode outNode = addNode("Chat Response");
						addAttribute(outNode, "Message", message.getResponse());

						addEdge(inNode, outNode, "triggers");
						addMessengerEdges(inNode, "generates");

						incomingMessages.put(intent, inNode);
					}
				}
				break;

			case SERVICE_ACCESS:
				AccessServiceFunction as = (AccessServiceFunction) function;

				if (as.getServiceType() instanceof OpenAPI) {
					try {
						OpenAPI oa = (OpenAPI) as.getServiceType();
						BotModelNode frameNode = addNode("Frame");
						addAttribute(frameNode, "Intent Keyword", oa.getIntent());

						BotModelNode actionNode = addNode("Bot Action");
						addAttribute(actionNode, "Action Type", "OpenAPI");
						addAttribute(actionNode, "FunctionName", oa.getFunctionName());
						addAttribute(actionNode, "Service Alias", oa.getBaseURL().toString());

						addEdge(frameNode, actionNode, "triggers");
						addMessengerEdges(frameNode, "generates");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (as.getServiceType() instanceof Las2peer) {

					try {
						Las2peer oa = (Las2peer) as.getServiceType();
						BotModelNode frameNode = addNode("Frame");
						addAttribute(frameNode, "Intent Keyword", oa.getIntent());

						BotModelNode actionNode = addNode("Bot Action");
						addAttribute(actionNode, "Action Type", "Service");
						addAttribute(actionNode, "FunctionName", oa.getFunctionName());
						addAttribute(actionNode, "Service Alias", oa.getServiceAlias());

						addEdge(frameNode, actionNode, "triggers");
						addMessengerEdges(frameNode, "generates");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

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
		String id = info.getAttrId(node.getType(), name);
		if (id == null)
			id = getID();
		attributes.put(id, attr);
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

		DrawingAlgorithm algorithm = new SpringEmbedders(model);
		algorithm.execute();

		return model;
	}

}
