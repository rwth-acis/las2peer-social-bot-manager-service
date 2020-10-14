package i5.las2peer.services.socialBotManagerService.parser.creation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.websocket.DeploymentException;

import i5.las2peer.api.Context;
import i5.las2peer.api.security.AgentException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.security.BotAgent;
import i5.las2peer.services.socialBotManagerService.chat.SlackEventChatMediator;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.BotConfiguration;
import i5.las2peer.services.socialBotManagerService.model.ChatResponse;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;
import i5.las2peer.services.socialBotManagerService.model.NLUKnowledge;
import i5.las2peer.services.socialBotManagerService.model.VLE;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import i5.las2peer.services.socialBotManagerService.parser.openapi.FrameMapper;
import i5.las2peer.tools.CryptoException;

public class Parser {

    private static final String botPass = "actingAgent";

    public i5.las2peer.services.socialBotManagerService.model.Bot parse(BotConfiguration config,
	    HashMap<String, BotAgent> botAgents,
	    i5.las2peer.services.socialBotManagerService.parser.creation.Bot data) {

	i5.las2peer.services.socialBotManagerService.model.Bot res = new i5.las2peer.services.socialBotManagerService.model.Bot();
	List<IncomingMessage> imList = new ArrayList<>();
	List<Frame> frameList = new ArrayList<>();

	res.setName(data.getName());

	// VLE Instance
	String vleAddress = "http://127.0.0.1:8070/";
	String vleName = "vleName";

	VLE vle = getVLEInstance(vleName, vleAddress);
	VLE r = config.getServiceConfiguration(vle.getName());
	if (r != null) {
	    for (Bot b : r.getBots().values()) {
		b.deactivateAll();
	    }
	}

	vle.addBot(res.getId(), res);
	res.setVle(vle);

	// Bot Agent
	String botName = data.getName();
	BotAgent botAgent = null;
	try {
	    try {
		botAgent = (BotAgent) Context.getCurrent()
			.fetchAgent(Context.getCurrent().getUserAgentIdentifierByLoginName(botName));
	    } catch (AgentNotFoundException e) {
		botAgent = BotAgent.createBotAgent(botPass);
		botAgent.unlock(botPass);
		botAgent.setLoginName(botName);
		Context.getCurrent().storeAgent(botAgent);
	    }
	    botAgent.unlock(botPass);
	    Context.getCurrent().registerReceiver(botAgent);
	} catch (AgentException | CryptoException e2) {
	    e2.printStackTrace();
	}
	System.out.println("Bot " + botName + " registered at: " + botAgent.getRunningAtNode().getNodeId());

	res.setId(botAgent.getIdentifier());
	res.setName(botAgent.getLoginName());
	botAgents.put(botName, botAgent);

	// NLU Knowledge
	NLUKnowledge nlu = new NLUKnowledge("DefaultNLU", "0", "http://localhost:5005");
	res.addRasaServer(nlu.getId(), nlu.getUrl());

	// Function
	for (Function function : data.getFunction()) {

	    // chit chat
	    if (function instanceof ChitChatFunction) {

		ChitChatFunction fn = (ChitChatFunction) function;
		for (Message mes : fn.getMessages()) {
		    String intent = mes.getIntent();
		    String nluId = "0";
		    String text = mes.getMessage();

		    ChatResponse response = new ChatResponse(text);
		    IncomingMessage im = new IncomingMessage(intent, nluId);
		    im.addResponse(response);
		    imList.add(im);
		}

	    }

	    // service access
	    if (function instanceof AccessServiceFunction) {

		AccessServiceFunction fn = (AccessServiceFunction) function;
		String serviceURL = fn.getServiceURL();
		String operationID = fn.getOperationID();
		String intent = fn.getIntent();

		FrameMapper mapper = new FrameMapper();
		Frame frame = mapper.create(serviceURL, operationID);
		frame.setIntent(intent);
		frameList.add(frame);
	    }

	}

	// Messenger
	for (Messenger item : data.getMessenger()) {
	    i5.las2peer.services.socialBotManagerService.model.Messenger messenger = null;
	    try {
		res.addMessenger(messenger);
	    } catch (IOException | DeploymentException | ParseBotException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	    String id = "";
	    String chatService = "";
	    String token = "";
	    SQLDatabase database = null;
	    String appID = "";

	    if (item instanceof TelegramMessenger) {
		TelegramMessenger obj = (TelegramMessenger) item;
		token = obj.getToken();
		chatService = obj.getType().toString();
	    }

	    if (item instanceof SlackMessenger) {
		SlackMessenger obj = (SlackMessenger) item;
		token = obj.getToken();
		chatService = obj.getType().toString();
		appID = obj.getAppId();
	    }

	    try {
		messenger = new i5.las2peer.services.socialBotManagerService.model.Messenger(id, chatService, token,
			database);
		messenger.addMessages(imList);
		messenger.addFrames(frameList);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    if (item instanceof SlackMessenger)
		((SlackEventChatMediator) messenger.getChatMediator()).setAppID(appID);
	}

	return res;
    }

    private VLE getVLEInstance(String name, String address) {

	VLE vle = new VLE();
	vle.setAddress(address);
	vle.setName(name);
	vle.setEnvironmentSeparator("singleEnvironment");

	return vle;
    }

}
