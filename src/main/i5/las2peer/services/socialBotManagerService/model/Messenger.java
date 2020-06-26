package i5.las2peer.services.socialBotManagerService.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import javax.websocket.DeploymentException;

import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.chat.RocketChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.SlackChatMediator;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class Messenger {
	private String name;

	private ChatMediator chatMediator;
	//private RasaNlu rasa;
    //private RasaNlu rasaAssessment;

	// Key: intent keyword
	private HashMap<String, IncomingMessage> knownIntents;

	// Used for keeping conversation state per channel
	private HashMap<String, IncomingMessage> stateMap;
    // Used for keeping context between assessment and non-assessment states
    // Key is the channelId
    private HashMap<String, String> context;
    // Used to know to which Function the received intents/messages are to be sent
    private HashMap<String, String> triggeredFunction;

	private Random random;
    

	public Messenger(String id, String chatService, String token,/* String rasaUrl, String rasaAssessmentUrl,*/ SQLDatabase database)
			throws IOException, DeploymentException, ParseBotException {

//		this.rasa = new RasaNlu(rasaUrl);
//        this.rasaAssessment = new RasaNlu(rasaAssessmentUrl);
		if (chatService.contentEquals("Slack")) {
			this.chatMediator = new SlackChatMediator(token);
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
        this.context = new HashMap<String, String>();
        this.triggeredFunction = new HashMap<String, String>();
	}

	public String getName() {
		return name;
	}

	public void addMessage(IncomingMessage msg) {
		this.knownIntents.put(msg.getIntentKeyword(), msg);
	}

	public ChatMediator getChatMediator() {
		return this.chatMediator;
	}
    // set the context of the specified channel
    public void setContext(String channel, String contextName){
        context.put(channel, contextName);
    }
    
    public void setContextToBasic(String channel){
        context.put(channel, "Basic");
    }    
    
    public String getContext(String channel){
        return this.context.get(channel);
    }

    
	// Handles simple responses ("Chat Response") directly, logs all messages and
	// extracted intents into `messageInfos` for further processing later on.
	// TODO: This would be much nicer if we could get a las2peer context here, but this
	// is usually called from the routine thread. Maybe a context can be shared across
	// threads somehow?
	public void handleMessages(ArrayList<MessageInfo> messageInfos, Bot bot) {
		Vector<ChatMessage> newMessages = this.chatMediator.getMessages();
		//System.out.println(newMessages.size());
		for (ChatMessage message : newMessages) {
			try {
                if(this.context.get(message.getChannel()) == null){
                    this.context.put(message.getChannel(), "Basic");
                } 
				Intent intent = null;
				// Special case: `!` commands
				// System.out.println(this.knownIntents.toString());
				if (message.getText().startsWith("!")) {
					// Split at first occurring whitespace
                    System.out.println("This was a command");
                
					String splitMessage[] = message.getText().split("\\s+", 2);

					// First word without '!' prefix
					String intentKeyword = splitMessage[0].substring(1);
					IncomingMessage incMsg = this.knownIntents.get(intentKeyword);
					// TODO: Log this? (`!` command with unknown intent / keyword)
					if (incMsg == null) {
						if(this.context.get(message.getChannel()) == "Basic") {
						continue;
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

					intent = new Intent(intentKeyword, entityKeyword, entityValue);
				} else {
                    // what if you want to start an assessment with a command? 
                    System.out.println("Intent Extraction now with  : " + this.context.get(message.getChannel()));
                    if( this.context.get(message.getChannel()) == "Basic" ){
                        intent = bot.getRasaServer("0").getIntent(message.getText());
                        System.out.println("Extracted Basic");
                    } else {
                        intent = bot.getRasaServer(context.get(message.getChannel())).getIntent(message.getText());
                    }                  
					
				}
                System.out.println(intent.getKeyword());
                 if(intent.getKeyword().equals("topicsQuestion")){
                    // TODO: too hard coded, find a way to do this in a prettier way
                    IncomingMessage states = this.stateMap.get(message.getChannel());
                    states = this.knownIntents.get("assessment");
                    System.out.println(states);
                    String triggeredFunctionIds =null; 
                    triggeredFunctionIds = states.getTriggeredFunctionId();
                    ServiceFunction botFunction = bot.getBotServiceFunctions().get(triggeredFunctionIds);
                    String answer = "The available topics are: ";
                    JSONParser parser = new JSONParser();
                    JSONObject content;
                    for(ServiceFunctionAttribute sfa : botFunction.getAttributes()){
                        // parse exception catcher here
                        content = (JSONObject) parser.parse(sfa.getContent());
                        System.out.println(content.getAsString("topic"));
                        answer += "\n" + content.getAsString("topic");
                    }
        
                    System.out.println(answer);
                    this.chatMediator.sendMessageToChannel(message.getChannel(), answer);
                   
                }
                // simple way, will need to take care of intents with same names but from different servers
            
                // Aaron: check if id is different than 0 , switch to respective nlu, context
				String triggeredFunctionId = null;
				IncomingMessage state = this.stateMap.get(message.getChannel());

				// No conversation state present, starting from scratch
				// TODO: Tweak this
				if (intent.getConfidence() >= 0.1f) {

					if (state == null) {
						state = this.knownIntents.get(intent.getKeyword());

						System.out.println(
								intent.getKeyword() + " detected with " + intent.getConfidence() + " confidence.");
						stateMap.put(message.getChannel(), state);
					} else {
						// any is a static forward
						// TODO include entities of intents
						if (state.getFollowingMessages() == null) {
							System.out.println("no follow up messages");
							state = this.knownIntents.get(intent.getKeyword());
							System.out.println(
									intent.getKeyword() + " detected with " + intent.getConfidence() + " confidence.");
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
								System.out.println(state.getIntentKeyword() + " set");
							}
						}
					}
				} else {
					System.out.println(
							intent.getKeyword() + " not detected with " + intent.getConfidence() + " confidence.");
					state = this.knownIntents.get("default");
					System.out.println(state.getIntentKeyword() + " set");
				}


					   
                    }
				}

				// No matching intent found, perform default action
                if(this.context.get(message.getChannel()) != "Basic"){
                    triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
                } else {
                    // problem mit chat response : wo soll der service call statt finden? 
                    if (state != null) {
                        ChatResponse response = state.getResponse(this.random);
                        System.out.println(state.getNluID());
                        if(state.getNluID() != ""){
                            System.out.println("NluId is : " + state.getNluID());
                            this.context.put(message.getChannel(), state.getNluID());
                            this.triggeredFunction.put(message.getChannel(), state.getTriggeredFunctionId());                            
                        }                        
                        if (response != null) {
                            if(response.getResponse() != ""){
                                this.chatMediator.sendMessageToChannel(message.getChannel(), response.getResponse());
                            } else {
                                if(response.getTriggeredFunctionId() != ""){
                                    this.triggeredFunction.put(message.getChannel(), response.getTriggeredFunctionId());
                                }
                            }
                        }
                        
                        if(this.context.get(message.getChannel()) != "Basic"){
                            triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
                        } else triggeredFunctionId = state.getTriggeredFunctionId();
                        System.out.println(triggeredFunctionId);
                        // If conversation flow is terminated, reset state
                        if (state.getFollowingMessages().isEmpty()) {
                            this.stateMap.remove(message.getChannel());
                        }
                    }
                }

				messageInfos.add(
						new MessageInfo(message, intent, triggeredFunctionId, bot.getName(), bot.getVle().getName()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
