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
import i5.las2peer.services.socialBotManagerService.nlu.FallbackNLU;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import i5.las2peer.services.socialBotManagerService.parser.creation.Bot;
import i5.las2peer.services.socialBotManagerService.parser.creation.Message;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.AccessServiceFunction;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.ChatFunction;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.ChitChatFunction;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.Function;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.Las2peer;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.OpenAPI;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.ServiceType;
import i5.las2peer.services.socialBotManagerService.parser.creation.messenger.Messenger;
import i5.las2peer.services.socialBotManagerService.parser.creation.messenger.SlackMessenger;
import i5.las2peer.services.socialBotManagerService.parser.creation.messenger.TelegramMessenger;
import i5.las2peer.services.socialBotManagerService.parser.drawing.DrawingAlgorithm;
import i5.las2peer.services.socialBotManagerService.parser.drawing.ScaffoldDrawing;
import i5.las2peer.services.socialBotManagerService.parser.nlg.NLGDataGroup;
import i5.las2peer.services.socialBotManagerService.parser.nlg.NLGTrainingData;
import io.swagger.util.Json;

public class BotModelParser {

	BotConfiguration config;

	LinkedHashMap<String, BotModelNode> nodeList = new LinkedHashMap<>();
	LinkedHashMap<String, BotModelEdge> edgeList = new LinkedHashMap<>();

	BotModelInfo info = new BotModelInfo();
	Map<BotModelNode, String> nodes = new LinkedHashMap<>();
	List<BotModelNode> messengers = new ArrayList<>();
	Map<String, BotModelNode> incomingMessages = new HashMap<>();
	BotModelNode botNode;

	public BotModelParser(BotConfiguration config) {
		this.config = config;
	}

	/**
	 * Reads an existing model into this parser Used to extend the existing model.
	 * 
	 * @param model
	 */
	public void read(BotModel model) {

		assert model != null : "read model is null";

		this.nodeList = model.getNodes();
		this.edgeList = model.getEdges();

		for (Entry<String, BotModelNode> entry : model.getNodes().entrySet()) {
			BotModelNode node = entry.getValue();
			String id = entry.getKey();
			String type = node.getType();

			this.nodes.put(node, id);

			if (type.contentEquals("Messenger"))
				this.messengers.add(node);

			if (type.contentEquals("Incoming Message")) {
				for (BotModelNodeAttribute attr : node.getAttributes().values())
					if (attr.getName().contentEquals("Intent")) {
						this.incomingMessages.put(attr.getValue().getValue(), node);
						System.out.println("add existing incoming Message for " + attr.getValue().getValue());
					}
			}

			if (type.contentEquals("Bot"))
				this.botNode = node;

		}

		assert this.botNode != null : "no bot node found";
		assert !this.messengers.isEmpty() : "no messenger found";
	}

	/**
	 * Parses a chat interfaces representation into a graphical representation
	 * 
	 * @param bot
	 * @return
	 */
	public BotModel parse(Bot bot) {

		assert bot != null;

		if (this.config == null)
			throw new IllegalStateException("parse bot without config");

		// VLE Instance
		String vleName = "vleName";
		String vleAddress = "http://127.0.0.1:8070/";
		String seperator = "";
		BotModelNode vleNode = addNode("Instance");
		addAttribute(vleNode, "Name", vleName);
		addAttribute(vleNode, "Address", vleAddress);
		addAttribute(vleNode, "Environment Separator", seperator);

		// Bot
		botNode = addNode("Bot");
		assert bot.getName() != null : "bot has no name";
		addAttribute(botNode, "Language", "English");
		addAttribute(botNode, "Name", bot.getName());
		addAttribute(botNode, "Description", bot.getDescription());
		addEdge(vleNode, botNode, "has");

		// NLU Knowledge
		String nluName = bot.getNluModule();
		LanguageUnderstander nlu = null;
		if (nluName != null)
			nlu = config.getNLU(nluName);
		else
			nlu = new FallbackNLU();
		assert nlu != null : "no nlu module found for " + bot.getNluModule();
		String nluID = "0";
		String nluURL = nlu.getUrl();

		BotModelNode nluNode = addNode("Knowledge");
		addAttribute(nluNode, "Type", "Understanding");
		addAttribute(nluNode, "Name", nluName);
		addAttribute(nluNode, "ID", nluID);
		addAttribute(nluNode, "URL", nluURL);
		addEdge(botNode, nluNode, "has");

		// Messenger
		for (Messenger messenger : bot.getMessenger())
			this.parse(messenger);

		// Function
		for (Function function : bot.getFunction())
			this.parse(function);

		return this.generate();
	}

	/**
	 * Generates a new bot model
	 * 
	 * @return new bot model
	 */
	public BotModel generate() {

		BotModel model = new BotModel();
		model.setAttributes(addBotModelAttributes());
		model.setNodes(nodeList);
		model.setEdges(edgeList);
		processNodes();

		return model;
	}

	/**
	 * Adds a function to a model
	 * 
	 * @param model
	 * @param function
	 * @return
	 */
	public void parse(ChatFunction function) {

		assert this.nodeList != null : "parse function into empty model";
		assert !this.nodeList.isEmpty() : "parse function into empty model";
		assert function != null : "function is null";

		// Function
		System.out.println("parse function of type Chat Function");

		for (Message message : function.getMessages()) {
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
	}

	/**
	 * Adds a function to a model
	 * 
	 * @param model
	 * @param function
	 * @return
	 */
	public void parse(ServiceType function) {

		assert this.nodeList != null : "parse function into empty model";
		assert !this.nodeList.isEmpty() : "parse function into empty model";
		assert function != null : "function is null";

		System.out.println(function.getClass());
		if (function instanceof OpenAPI) {
			try {
				OpenAPI oa = (OpenAPI) function;
				BotModelNode frameNode = addNode("Frame");
				addAttribute(frameNode, "Intent Keyword", oa.getIntent());
				addAttribute(frameNode, "Operation Name", oa.getIntent());

				BotModelNode actionNode = addNode("Bot Action");
				addAttribute(actionNode, "Action Type", "OpenAPI");
				addAttribute(actionNode, "Function Name", oa.getFunctionName());
				addAttribute(actionNode, "Service Alias", oa.getBaseURL().toString());

				addEdge(frameNode, actionNode, "triggers");
				addMessengerEdges(frameNode, "generates");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		else if (function instanceof Las2peer) {

			try {
				Las2peer oa = (Las2peer) function;
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
		} else {
			System.out.println("unknown action type");
		}

	}

	/**
	 * Adds a function to a model
	 * 
	 * @param model
	 * @param function
	 * @return
	 */
	public void parse(Function function) {

		assert this.nodeList != null : "parse function into empty model";
		assert !this.nodeList.isEmpty() : "parse function into empty model";
		assert function != null : "function is null";

		// Function
		System.out.println("parse function of type " + function.getType());
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

			Json.prettyPrint(as);
			System.out.println(as.getServiceType());
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

			else if (as.getServiceType() instanceof Las2peer) {

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
			} else {
				System.out.println("unknown action type");
			}

			break;
		default:
			assert false : "no known function" + function.getType();
			break;

		}
	}

	public void parse(Messenger messenger) {

		assert messenger != null;
		assert botNode != null;
		System.out.println("parse messenger " + messenger.getType());

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

	/**
	 * @param model
	 * @param data
	 * @return
	 */
	public void parse(NLGTrainingData data) {

		// search Knowledge Node
		BotModelNode nlgNode = null;
		for (BotModelNode node : this.nodeList.values())
			if (node.getType().contentEquals("Knowledge"))
				for (BotModelNodeAttribute attr : node.getAttributes().values())
					if (attr.getName().contentEquals("Type") && attr.getValue().getValue().contentEquals("Generation"))
						nlgNode = node;

		// add Knowledge Node
		if (nlgNode == null) {
			nlgNode = this.addNode("Knowledge");
			this.addAttribute(nlgNode, "Name", "abcNLG");

			for (BotModelNode node1 : this.nodeList.values()) {
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

	}

	public BotModelNode addNode(String type) {

		System.out.println("add graphical element of type " + type);
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

	/**
	 * Gets a Id for an model element
	 * 
	 * @return a random ID
	 */
	public String getID() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 24);
	}

	/**
	 * Gives the model a graphical layout
	 * 
	 * @param model
	 * @return
	 */
	public BotModel order(BotModel model) {

		DrawingAlgorithm algorithm = new ScaffoldDrawing(model);
		algorithm.execute();

		return model;
	}

	/**
	 * Return the BotModel in JSON format
	 * 
	 * @param botModel
	 * @return
	 */
	public String asJSON(BotModel botModel) {
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

}
