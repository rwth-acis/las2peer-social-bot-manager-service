package i5.las2peer.services.socialBotManagerService.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import javax.websocket.DeploymentException;

import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.chat.ChatService;
import i5.las2peer.services.socialBotManagerService.chat.RocketChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.SlackEventChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.TelegramChatMediator;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.dialogue.AbstractDialogueManager;
import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueManagerGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueManagerType;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;

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
     * This map contains all started dialogue. The key is the channel id
     */
    private HashMap<String, Dialogue> openDialogues;

    /**
     * This map contains all known frames. The key is the intent keyword
     */
    private HashMap<String, Frame> intentFrames;

    /**
     * This map contains all known incoming message intents. The key is the intent keyword
     */
    private HashMap<String, IncomingMessage> knownIntents;
    
    /**
     * Dialogue Manager of this messenger
     */
    private AbstractDialogueManager dialogueManager;

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

	this.chatService = ChatService.fromString(chatService);
	System.out.println("Messenger: " + chatService.toString());
	if (chatService.contentEquals("Slack")) {
	    this.chatMediator = new SlackEventChatMediator(token);
	} else if (chatService.contentEquals("Telegram")) {
	    this.chatMediator = new TelegramChatMediator(token);
	} else if (chatService.contentEquals("Rocket.Chat")) {
	    this.chatMediator = new RocketChatMediator(token, database, new RasaNlu("rasaUrl"));
	} else { // TODO: Implement more backends
	    throw new ParseBotException("Unimplemented chat service: " + chatService);
	}
	this.name = id;
	this.knownIntents = new HashMap<String, IncomingMessage>();
	this.stateMap = new HashMap<String, IncomingMessage>();
	this.random = new Random();
	// Initialize the assessment setup
	this.currentNluModel = new HashMap<String, String>();
	this.triggeredFunction = new HashMap<String, String>();
	// Dialogue Manager
	DialogueManagerGenerator generator = new DialogueManagerGenerator();
	this.dialogueManager = generator.generate(DialogueManagerType.AGENDA_TREE, this);	
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

	if (this.currentNluModel.get(message.getChannel()) == null) {
	    this.currentNluModel.put(message.getChannel(), "0");
	}
	
	 System.out.println("Message Text  : " + message.getText());
	 System.out.println("Message Channel  : " + message.getChannel());
	 System.out.println("Intent Extraction now with  : " + this.currentNluModel.get(message.getChannel()));
	 Intent intent = bot.getRasaServer(currentNluModel.get(message.getChannel())).getIntent(message.getText());
	    
	    
	String channel = message.getChannel();
	MessageInfo info = new MessageInfo();
	info.intent = intent;
	info.message = message;

	String response = null;
	// Open Dialogues
	if (this.openDialogues.containsKey(channel)) {
	    System.out.println("resume open dialogue: " + message.getChannel());
	    response = this.openDialogues.get(channel).handle(info);
	} else {
	    DialogueManagerGenerator generator = new DialogueManagerGenerator();
	    AbstractDialogueManager manager = generator.generate(DialogueManagerType.AGENDA_TREE, this);
	    Dialogue dialogue = new Dialogue(manager);
	    this.openDialogues.put(channel, dialogue);
	    response = dialogue.handle(info);
	    System.out.println("start new dialogue: " + message.getChannel());
	}	
	
	this.getChatMediator().sendMessageToChannel(channel, response);
	
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
				state = state.getFollowingMessages().get(intent.getKeyword());
				stateMap.put(message.getChannel(), state);
			    } else {
				System.out.println(intent.getKeyword() + " not found in state map. Confidence: "
					+ intent.getConfidence() + " confidence.");
				// try any
				if (state.getFollowingMessages().get("any") != null) {
				    state = state.getFollowingMessages().get("any");
				    stateMap.put(message.getChannel(), state);
				} else {
				    state = this.knownIntents.get("default");

				    // System.out.println(state.getIntentKeyword() + " set");
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
				String split = "";
				// allows users to use linebreaks \n during the modeling for chat responses
				for (int i = 0; i < response.getResponse().split("\\\\n").length; i++) {
				    System.out.println(i);
				    split += response.getResponse().split("\\\\n")[i] + " \n ";
				}
				System.out.println(split);
				this.chatMediator.sendMessageToChannel(message.getChannel(), split);
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
		/*
		 * if(message.getEmail() == null) {
		 * message.setEmail(this.getEmail(message.getChannel())); }
		 */
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
	if(this.intentFrames != null)	 
	    return this.intentFrames.values();
	return null;
    }
    
    public Collection<IncomingMessage> getIncomingMessages() {
	return this.knownIntents.values();
    }
    
    public HashMap<String, Dialogue> getOpenDialogues() {
	return this.openDialogues;
    }

    public void setOpenDialogues(HashMap<String, Dialogue> openDialogues) {
	this.openDialogues = openDialogues;
    }
}