package i5.las2peer.services.socialBotManagerService.model;

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

public class Messenger {
	private String name;

	private ChatMediator chatMediator;
	private RasaNlu rasa;
    private RasaNlu rasaAssessment;

	// Key: intent keyword
	private HashMap<String, IncomingMessage> knownIntents;

	// Used for keeping conversation state per channel
	private HashMap<String, IncomingMessage> stateMap;
    // Used for keeping context between assessment and non-assessment states
    // Key is the channelId
    private HashMap<String, String> context;
    // Used to keep track at which question one currently is of the given assessment
    // Key is the channelId
    private HashMap<String, Integer> currQuestion;
    // Used to keep the assessment that is currently being done
    // Key is the channelId 
    private HashMap<String, String[][]> currAssessment;

	private Random random;
    

	public Messenger(String id, String chatService, String token, String rasaUrl, String rasaAssessmentUrl, SQLDatabase database)
			throws IOException, DeploymentException, ParseBotException {

		this.rasa = new RasaNlu(rasaUrl);
        this.rasaAssessment = new RasaNlu(rasaAssessmentUrl);
		if (chatService.contentEquals("Slack")) {
			this.chatMediator = new SlackChatMediator(token);
		} else if (chatService.contentEquals("Rocket.Chat")) {
			this.chatMediator = new RocketChatMediator(token, database, this.rasa);
		} else { // TODO: Implement more backends
			throw new ParseBotException("Unimplemented chat service: " + chatService);
		}
		this.name = id;

		this.knownIntents = new HashMap<String, IncomingMessage>();
		this.stateMap = new HashMap<String, IncomingMessage>();
        // Used to quit the Assessment, TODO: maybe think about a way to include this during the Modeling ? 
        IncomingMessage quit = new IncomingMessage("quit");
        this.knownIntents.put("quit", quit);
		this.random = new Random();
        // Initialize the assessment setup
        this.context = new HashMap<String, String>();
        this.currQuestion = new HashMap<String, Integer>();
        this.currAssessment = new HashMap<String, String[][]>();
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
    
    public String getContext(String channel){
        return this.context.get(channel);
    }
    // will prepare an assessment for the specified channel -> prepare the numbers/questions/intent/hints and follow up by asking the first question
    public void setUpCurrentAssessment(String content, String channel){
        String[] contentList = content.split(";");
        String topic = contentList[0];
        int length = (contentList.length-1)/4; 
        int noNum = 0;
        String[][] assessmentContent = new String[length][4];
        for(int i = 0; i < length ; i++){
            if(contentList[i*4+1]==""){
                noNum++;    
            }
            assessmentContent[i][0] = contentList[i*4+1];
            assessmentContent[i][1] = contentList[i*4+2];
            assessmentContent[i][2] = contentList[i*4+3];
            assessmentContent[i][3] = contentList[i*4+4];     
        }
        int[] sequence = new int[length];
        this.currAssessment.put(channel, assessmentContent);
        this.currQuestion.put(channel, 0);
        System.out.println("Curr number is :" + this.currQuestion.get(channel));
        for(int i = 0; i < length ; i++){
            System.out.println(assessmentContent[i][0] + " " + assessmentContent[i][1] + " " + assessmentContent[i][2] + " " + assessmentContent[i][3]);
        } 
        System.out.println(channel);
        this.chatMediator.sendMessageToChannel(channel, assessmentContent[0][1]);
    }
    
    private void assess(String channel, String intent){
        System.out.println(this.currQuestion.get(channel));
        System.out.println(this.currAssessment.get(channel)[this.currQuestion.get(channel)][2]);
        if(intent.equals(this.currAssessment.get(channel)[this.currQuestion.get(channel)][2])){
            this.chatMediator.sendMessageToChannel(channel, "Correct Answer!");
        } else this.chatMediator.sendMessageToChannel(channel, "Wrong Answer:/");
        this.currQuestion.put(channel,this.currQuestion.get(channel)+1);
        if(this.currQuestion.get(channel)==this.currAssessment.get(channel).length){
            this.chatMediator.sendMessageToChannel(channel, "Assessment is over, Good job");
            setContext(channel, "Basic");
        } else {
            this.chatMediator.sendMessageToChannel(channel, this.currAssessment.get(channel)[this.currQuestion.get(channel)][1]);        
        }
        
        
        
        
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
                if(this.context.get(message.getChannel())== null){
                    this.context.put(message.getChannel(), "Basic");
                } 
				Intent intent = null;
				// Special case: `!` commands
				if (message.getText().startsWith("!")) {
					// Split at first occurring whitespace
                    System.out.println("This was a command");
                
					String splitMessage[] = message.getText().split("\\s+", 2);

					// First word without '!' prefix
					String intentKeyword = splitMessage[0].substring(1);
					IncomingMessage incMsg = this.knownIntents.get(intentKeyword);
					// TODO: Log this? (`!` command with unknown intent / keyword)
					if (incMsg == null) {
						continue;
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
                    if( this.context.get(message.getChannel()) == "Basic" ){
                        intent = this.rasa.getIntent(message.getText());
                    } else if( this.context.get(message.getChannel()) == "Assessment" ){
                        intent = this.rasaAssessment.getIntent(message.getText());
                        assess(message.getChannel(),intent.getKeyword());
                    }                  
					
				}
                System.out.println(intent.getKeyword());
               /* if(intent.getKeyword().equals("assessment")){
                    if( this.context.get(message.getChannel()) == "Basic" ){
                        context.put(message.getChannel(), "Ass");
                        System.out.println("I am in Assessment Mode now :)");
                    } 
                    
                } else*/
                if(intent.getKeyword().equals("quit")){
                    if( this.context.get(message.getChannel()) == "Ass" ){
                        context.put(message.getChannel(), "Basic");
                        System.out.println("I just left Assessment Mode");
                    } else {
                        this.chatMediator.sendMessageToChannel(message.getChannel(), "No Assessment to quit");
                    }
                } else if(intent.getKeyword().equals("topicsQuestion")){
                    // TODO: too hard coded, find a way to do this in a prettier way
                    IncomingMessage states = this.stateMap.get(message.getChannel());
                    states = this.knownIntents.get("assessment");
                    System.out.println(states);
                    String triggeredFunctionIds =null; 
                    triggeredFunctionIds = states.getTriggeredFunctionId();
                    ServiceFunction botFunction = bot.getBotServiceFunctions().get(triggeredFunctionIds);
                    String answer = "The available topics are: ";
                    for(ServiceFunctionAttribute sfa : botFunction.getAttributes()){
                        System.out.println(sfa.getNluQuizContent().split(";")[0]);
                        answer += "\n" + sfa.getNluQuizContent().split(";")[0];
                    }
        
                    System.out.println(answer);
                    this.chatMediator.sendMessageToChannel(message.getChannel(), answer);
                   
                }
                
				String triggeredFunctionId = null;
				IncomingMessage state = this.stateMap.get(message.getChannel());

				// No conversation state present, starting from scratch
				if (state == null) {
					// TODO: Tweak this
					if (intent.getConfidence() >= 0.1f) {
						state = this.knownIntents.get(intent.getKeyword());
					}
				}

				// No matching intent found, perform default action
				if (state == null) {
					state = this.knownIntents.get("default");
				}

				if (state != null) {
					String response = state.getResponse(this.random);
					if (response != null) {
						this.chatMediator.sendMessageToChannel(message.getChannel(), response);
					}
					triggeredFunctionId = state.getTriggeredFunctionId();
                    System.out.println(triggeredFunctionId);
					// If conversation flow is terminated, reset state
					if (state.getFollowingMessages().isEmpty()) {
						this.stateMap.remove(message.getChannel());
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
