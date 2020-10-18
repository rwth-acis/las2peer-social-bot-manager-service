package i5.las2peer.services.socialBotManagerService.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.websocket.DeploymentException;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.chat.ChatService;
import i5.las2peer.services.socialBotManagerService.chat.EventChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.RocketChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.SlackEventChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.TelegramChatMediator;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.DialogueManagerGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class Messenger {

    /**
     * The name of this messenger object. For Slack this is the application id.
     */
    private String name = "";

    /**
     * The messenger application provider this object corresponds to
     */
    private ChatService chatService;

    /**
     * The chat mediator connects to the external messenger application
     */
    private ChatMediator chatMediator;

    /**
     * This map contains all domains of this messenger
     */
    private HashMap<String, Domain> domains;

    /**
     * This map contains all started dialogue. The key is the channel id
     */
    private HashMap<String, Dialogue> openDialogues;

    /**
     * This map contains all known frames. The key is the intent keyword
     */
    private HashMap<String, Frame> intentFrames;

    /**
     * This map contains all known incoming message intents. The key is the intent
     * keyword
     */
    private HashMap<String, IncomingMessage> knownIntents;

    // Used for keeping conversation state per channel
    private HashMap<String, IncomingMessage> stateMap;
    // Used for keeping context between assessment and non-assessment states
    // Key is the channelId
    private HashMap<String, String> currentNluModel;
    // Used to know to which Function the received intents/messages are to be sent
    // Is additionally used to check if we are currently communicating with a
    // service(if set, then yes otherwise no)
    private HashMap<String, String> triggeredFunction;
    // dont think i need this one
    // private HashMap<String, Bool> contextWithService;

    private Random random;

    public Messenger(String id, String chatService, String token, SQLDatabase database)
	    throws IOException, DeploymentException, ParseBotException {

	this.name = id;

	// Chat Mediator
	this.chatService = ChatService.fromString(chatService);
	System.out.println("Messenger: " + chatService.toString());
	switch (this.chatService) {
	case SLACK:
	    this.chatMediator = new SlackEventChatMediator(token);
	    break;
	case TELEGRAM:
	    this.chatMediator = new TelegramChatMediator(token);
	    break;
	case ROCKET_CHAT:
	    this.chatMediator = new RocketChatMediator(token, database, new RasaNlu("rasaUrl"));
	    break;
	default:
	    throw new ParseBotException("Unimplemented chat service: " + chatService);
	}

	// State map
	this.knownIntents = new HashMap<String, IncomingMessage>();
	this.stateMap = new HashMap<String, IncomingMessage>();
	this.random = new Random();

	// Initialize the assessment setup
	this.currentNluModel = new HashMap<String, String>();
	this.triggeredFunction = new HashMap<String, String>();

	// Dialogue Manager
	DialogueManagerGenerator generator = new DialogueManagerGenerator();
	this.intentFrames = new HashMap<String, Frame>();
	this.openDialogues = new HashMap<String, Dialogue>();
    }

    public String getName() {
	return name;
    }

    public void addMessage(IncomingMessage msg) {
	this.knownIntents.put(msg.getIntentKeyword(), msg);
    }

    public void addFrame(Frame frame) {
	this.intentFrames.put(frame.getIntent(), frame);
    }

    public ChatMediator getChatMediator() {
	return this.chatMediator;
    }
    // set the context of the specified channel
    /*
     * public void setContext(String channel, String contextName){
     * context.put(channel, contextName);
     * 
     * }
     */

    /*
     * public String getEmail(String channel) throws IOException, SlackApiException
     * { return chatMediator.getEmail(channel); };
     */

    public void setContextToBasic(String channel) {
	triggeredFunction.remove(channel);

	IncomingMessage state = this.stateMap.get(channel);
	if (state != null) {
	    if (state.getFollowingMessages() == null) {
		System.out.println("Conversation flow ended now");
	    } else {
		state = state.getFollowingMessages().get("");
		stateMap.put(channel, state);
		this.chatMediator.sendMessageToChannel(channel, state.getResponse(random).getResponse());

	    }
	}
    }

    public String getContext(String channel) {
	return this.triggeredFunction.get(channel);
    }

    public MessageInfo handleMessage(ChatMessage message, Bot bot) {

	if (message == null || message.getText() == null)
	    return null;

	if (this.currentNluModel.get(message.getChannel()) == null) {
	    this.currentNluModel.put(message.getChannel(), "0");
	}

	System.out.println("Message Text  : " + message.getText());
	System.out.println("Message Channel  : " + message.getChannel());

	Intent intent = null;
	if (message.hasCommand()) {
	    System.out.println("treat command as intent: " + message.getCommand());
	    intent = new Intent(message.getCommand(), 1.0f);
	} else {

	System.out.println("Intent Extraction now with  : " + this.currentNluModel.get(message.getChannel()));
	    intent = bot.getRasaServer(currentNluModel.get(message.getChannel())).getIntent(message.getText());
	}

	String channel = message.getChannel();
	MessageInfo info = new MessageInfo();
	info.intent = intent;
	info.message = message;

	ResponseMessage response = null;
	// Open Dialogues
	if (this.openDialogues.containsKey(channel)) {
	    System.out.println("resume open dialogue: " + message.getChannel());
	    response = this.openDialogues.get(channel).handle(info);

	} else {
	    Dialogue dialogue = new Dialogue(this);
	    this.openDialogues.put(channel, dialogue);
	    response = dialogue.handle(info);
	    System.out.println("start new dialogue: " + message.getChannel());
	}

	EventChatMediator medi;
	if (!response.hasButtons())
	    this.getChatMediator().sendMessageToChannel(channel, response.getMessage());
	else {
	    medi = (EventChatMediator) this.getChatMediator();
	    medi.sendMessageToChannel(channel, response);
	}

	MessageInfo messageInfo = new MessageInfo();

	return messageInfo;
    }

    // Handles simple responses ("Chat Response") directly, logs all messages and
    // extracted intents into `messageInfos` for further processing later on.
    // TODO: This would be much nicer if we could get a las2peer context here, but
    // this
    // is usually called from the routine thread. Maybe a context can be shared
    // across
    // threads somehow?
    public void handleMessages(ArrayList<MessageInfo> messageInfos, Bot bot) {
	Vector<ChatMessage> newMessages = this.chatMediator.getMessages();
	for (ChatMessage message : newMessages) {
	    try {
		if (this.currentNluModel.get(message.getChannel()) == null) {
		    this.currentNluModel.put(message.getChannel(), "0");
		}
		Intent intent = null;

		// Special case: `!` commands
		// System.out.println(this.knownIntents.toString());
		if (message.getText().startsWith("!")) {
		    intent = handleCommand(message);

		} else {
		    // what if you want to start an assessment with a command?
		    System.out.println("Message Text  : " + message.getText());
		    System.out.println("Message Channel  : " + message.getChannel());
		    System.out
			    .println("Intent Extraction now with  : " + this.currentNluModel.get(message.getChannel()));
		    // System.out
		    // .println("Rasa Server : " +
		    // bot.getRasaServer(currentNluModel.get(message.getChannel())));
		    // System.out.println("Intent : " +
		    // bot.getRasaServer(currentNluModel.get(message.getChannel()))
		    // .getIntent(message.getText()));
		    intent = bot.getRasaServer(currentNluModel.get(message.getChannel())).getIntent(message.getText());

		}
		System.out.println(intent.getKeyword());
		String triggeredFunctionId = null;
		IncomingMessage state = this.stateMap.get(message.getChannel());
		if (state != null) {
		    System.out.println(state.getIntentKeyword());

		}
		// No conversation state present, starting from scratch
		// TODO: Tweak this

		if (!this.triggeredFunction.containsKey(message.getChannel())) {

		    if (intent.getConfidence() >= 0.40f) {
			if (state == null) {
			    state = this.knownIntents.get(intent.getKeyword());
			    System.out.println(
				    intent.getKeyword() + " detected with " + intent.getConfidence() + " confidence.");
			    stateMap.put(message.getChannel(), state);
			} else {
			    // any is a static forward
			    // TODO include entities of intents
			    if (state.getFollowingMessages() == null || state.getFollowingMessages().isEmpty()) {
				System.out.println("no follow up messages");
				state = this.knownIntents.get(intent.getKeyword());
				this.currentNluModel.put(message.getChannel(), "0");
				System.out.println(intent.getKeyword() + " detected with " + intent.getConfidence()
					+ " confidence.");
				stateMap.put(message.getChannel(), state);
			    } else if (state.getFollowingMessages().get(intent.getKeyword()) != null) {
				System.out.println("try follow up message");
				// check ratings
				String keyword = intent.getKeyword();
				String txt = message.getText();
				if (keyword.equals("highrating")
					&& (txt.equals("1") || txt.equals("2") || txt.equals("3"))) {
				    keyword = "lowrating";
				} else if (keyword.equals("lowrating") && (txt.equals("4") || txt.equals("5"))) {
				    keyword = "highrating";
				}
				state = state.getFollowingMessages().get(intent.getKeyword());
				stateMap.put(message.getChannel(), state);
			    } else {
				System.out.println(intent.getKeyword() + " not found in state map. Confidence: "
					+ intent.getConfidence() + " confidence.");
				// try any
				if (state.getFollowingMessages().get("any") != null) {
				    state = state.getFollowingMessages().get("any");
				    stateMap.put(message.getChannel(), state);
				    String tmp = message.getText().replaceAll("[^0-9]", "");
				    if (tmp.length() > 0 && state.getIntentKeyword().contains("showtasks")) {
					// try to get tasknumber
					int t = Integer.parseInt(tmp);
					if ((message.getRole() % 2) == (t % 2) && t < 9) {
					    state = knownIntents.get("t" + tmp);
					} else {
					    state = state.getFollowingMessages().get("any");
					}
				    } else if (state.getIntentKeyword().contains("functions")) {
					if (message.getText().equals("a") || message.getText().equals("a)")
						|| message.getText().contains("anzeigen")) {
					    state = knownIntents.get("showtasks" + message.getRole());
					} else if (message.getText().equals("b") || message.getText().equals("b)")
						|| message.getText().contains("abgeben")) {
					    state = knownIntents.get("submission");
					} else if (message.getText().equals("c") || message.getText().equals("c)")
						|| message.getText().contains("Feedback")) {
					    state = knownIntents.get("userfeedback");
					}
				    } else {
					state = state.getFollowingMessages().get("any");
				    }
				    stateMap.put(message.getChannel(), state);
				} else {
				    state = this.knownIntents.get("default");
				}
			    }
			}
		    } else {
			System.out.println(
				intent.getKeyword() + " not detected with " + intent.getConfidence() + " confidence.");
			state = this.knownIntents.get("default");
			System.out.println(state.getIntentKeyword() + " set");
		    }
		} else if (intent.getConfidence() < 0.40f) {
		    intent = new Intent("default", "", "");
		}

		// tud
		if ((intent.getKeyword().equals("zeige") || intent.getKeyword().equals("hast")
			|| intent.getKeyword().equals("will"))
			&& !this.triggeredFunction.containsKey(message.getChannel())) {
		    if (intent.getEntity("muster") != null) {
			state = this.knownIntents.get("mustertext");
		    } else if (intent.getEntity("video") != null) {
			state = this.knownIntents.get("video");
		    } else if (intent.getEntity("help") != null) {
			state = this.knownIntents.get("help");
		    } else if (intent.getEntity("pause") != null) {
			state = this.knownIntents.get("pause");
		    } else if (intent.getEntity("upload") != null) {
			state = this.knownIntents.get("upload");
		    } else if (intent.getEntity("schreibaufgabe") != null) {
			state = this.knownIntents.get("beschreibung");
		    } else {
			state = this.knownIntents.get("default");
		    }
		}

		// ul
		else if (intent.getEntities().size() > 0 && !this.triggeredFunction.containsKey(message.getChannel())) {
		    Collection<Entity> entities = intent.getEntities();
		    System.out.println("try to use entity...");
		    for (Entity e : entities) {
			System.out.println(e.getEntityName() + " (" + e.getValue() + ")");
			state = this.knownIntents.get(e.getEntityName());
			stateMap.put(message.getChannel(), state);
		    }
		}

		Boolean contextOn = false;
		// No matching intent found, perform default action
		if (this.triggeredFunction.containsKey(message.getChannel())) {
		    triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
		    contextOn = true;
		} else {
		    if (state != null) {
			ChatResponse response = null;
			if (intent.getEntitieValues().size() == 1) {
			    for (ChatResponse res : state.getResponseArray()) {
				System.out.println(res.getTriggerEntity());
				if (res.getTriggerEntity().equals(intent.getEntitieValues().get(0))) {
				    response = res;
				}
			    }
			}
			if (response == null) {
			    response = state.getResponse(this.random);
			}
			if (state.getNluID() != "") {
			    System.out.println("New NluId is : " + state.getNluID());
			    this.currentNluModel.put(message.getChannel(), state.getNluID());
			}
			if (response != null) {
			    if (response.getResponse() != "") {
				if (intent.getEntity("schreibaufgabe") != null
					|| intent.getKeyword().equals("beschreibung")) {
				    File f = new File("Schreibauftrag.pdf");
				    this.chatMediator.sendFileMessageToChannel(message.getChannel(), f,
					    response.getResponse());
				} else if (intent.getEntity("muster") != null
					|| intent.getKeyword().equals("mustertext")) {
				    File f = new File("Mustertext.pdf");
				    this.chatMediator.sendFileMessageToChannel(message.getChannel(), f,
					    response.getResponse());
				} else if (state.getIntentKeyword().equals("suggestMaterial")) {
				    // chatbot wl
				    String text = message.getText();
				    String[] words = text.split(",");
				    MiniClient client = new MiniClient();
				    client.setConnectorEndpoint("http://137.226.232.175:32303");

				    HashMap<String, String> headers = new HashMap<String, String>();
				    int counter = 0;
				    String s = "";
				    for (int i = 0; i < words.length; i++) {
					JSONObject body = new JSONObject();
					JSONArray terms = new JSONArray();
					terms.add(words[i].trim());
					body.put("terms", terms);
					ClientResponse r = client.sendRequest("POST", "materials", body.toJSONString(),
						"application/json", "application/json", headers);

					JSONParser p = new JSONParser();
					JSONObject result = (JSONObject) p.parse(r.getResponse());
					if (result.keySet().size() > 1) {
					    counter++;
					    JSONArray materials = (JSONArray) result.get("@graph");
					    for (Object j : materials) {
						JSONObject jo = (JSONObject) j;
						s += "\\n" + words[i] + ": [" + jo.getAsString("title") + "]("
							+ jo.getAsString("link") + ")";
					    }
					}
				    }
				    // response = response.replace("$X", "" + s);
				    this.chatMediator.sendMessageToChannel(message.getChannel(),
					    response.getResponse().replace("$X", "" + s));
				} else if (state.getIntentKeyword().equals("liste")) {
				    String text = message.getText();
				    String[] words = text.split(",");
				    JSONArray wordsCleaned = new JSONArray();
				    for (int i = 0; i < words.length; i++) {
					wordsCleaned.add(words[i].trim());
				    }
				    MiniClient client = new MiniClient();
				    client.setConnectorEndpoint("http://137.226.232.175:32303");

				    HashMap<String, String> headers = new HashMap<String, String>();

				    JSONObject body = new JSONObject();
				    body.put("terms", wordsCleaned);
				    ClientResponse r = client.sendRequest("POST", "compare", body.toJSONString(),
					    "application/json", "application/json", headers);

				    JSONParser p = new JSONParser();
				    JSONObject result = (JSONObject) p.parse(r.getResponse());

				    // response = response.replace("$X", result.getAsString("matchCount"));
				    this.chatMediator.sendMessageToChannel(message.getChannel(),
					    response.getResponse().replace("$X", result.getAsString("matchCount")));
				} else if (state.getIntentKeyword().equals("showtasks")) {
				    if (message.getRole() % 2 == 1) {
					state = this.knownIntents.get("showtasks1");
				    } else {
					state = this.knownIntents.get("showtasks2");
				    }
				    response = state.getResponse(this.random);
				    this.chatMediator.sendMessageToChannel(message.getChannel(),
					    response.getResponse());
				} else {
				    String split = "";
				    // allows users to use linebreaks \n during the modeling for chat responses
				    for (int i = 0; i < response.getResponse().split("\\\\n").length; i++) {
					System.out.println(i);
					split += response.getResponse().split("\\\\n")[i] + " \n ";
				    }
				    System.out.println(split);
				    this.chatMediator.sendMessageToChannel(message.getChannel(), split);
				}
			    } else {
				if (response.getTriggeredFunctionId() != "") {
				    this.triggeredFunction.put(message.getChannel(), response.getTriggeredFunctionId());
				    contextOn = true;
				} else {
				    System.out.println("No Bot Action was given to the Response");
				}
			    }
			}
			if (this.triggeredFunction.containsKey(message.getChannel())) {
			    triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
			} else
			    triggeredFunctionId = state.getTriggeredFunctionId();
			// If conversation flow is terminated, reset state
			if (state.getFollowingMessages().isEmpty()) {
			    this.stateMap.remove(message.getChannel());
			}
		    }
		}
		messageInfos.add(new MessageInfo(message, intent, triggeredFunctionId, bot.getName(),
			bot.getVle().getName(), contextOn));
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return;
    }

    public Intent handleCommand(ChatMessage message) {

	// Split at first occurring whitespace
	System.out.println("This was a command");
	String splitMessage[] = message.getText().split("\\s+", 2);

	// First word without '!' prefix
	String intentKeyword = splitMessage[0].substring(1);
	IncomingMessage incMsg = this.knownIntents.get(intentKeyword);
	// TODO: Log this? (`!` command with unknown intent / keyword)
	if (incMsg == null) {
	    if (this.currentNluModel.get(message.getChannel()) == "0") {
		return null;
	    } else {
		incMsg = new IncomingMessage(intentKeyword, "");
		incMsg.setEntityKeyword("newEntity");
	    }
	}

	String entityKeyword = incMsg.getEntityKeyword();
	String entityValue = null;
	// Entity value is the rest of the message. The whole rest
	// is in the second element, since we only split it into two parts.
	if (splitMessage.length > 1) {
	    entityValue = splitMessage[1];
	}

	return (new Intent(intentKeyword, entityKeyword, entityValue));
    }

    public ChatService getChatService() {
	return chatService;
    }

    public void setChatService(ChatService chatService) {
	this.chatService = chatService;
    }

    public Collection<Frame> getFrames() {
	if (this.intentFrames != null)
	    return this.intentFrames.values();
	return null;
    }

    public void addDomain(Domain domain) {
	this.domains.put(domain.getName(), domain);
    }

    public Collection<IncomingMessage> getIncomingMessages() {
	return this.knownIntents.values();
    }

    public void addMessages(List<IncomingMessage> list) {
	for (IncomingMessage message : list) {
	    this.addMessage(message);
	}
    }

    public void addFrames(List<Frame> frameList) {
	for (Frame frame : frameList) {
	    this.addFrame(frame);
	}
    }

    public HashMap<String, Dialogue> getOpenDialogues() {
	return this.openDialogues;
    }

    public void setOpenDialogues(HashMap<String, Dialogue> openDialogues) {
	this.openDialogues = openDialogues;
    }

}