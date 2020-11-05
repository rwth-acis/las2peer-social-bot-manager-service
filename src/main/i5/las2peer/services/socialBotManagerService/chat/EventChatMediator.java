package i5.las2peer.services.socialBotManagerService.chat;

import java.util.Vector;

import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import net.minidev.json.JSONObject;

public abstract class EventChatMediator extends ChatMediator {

    public EventChatMediator(String authToken) {
	super(authToken);
    }

    public abstract ChatMessage handleEvent(JSONObject parsedEvent);

    @Override
    public abstract boolean sendMessageToChannel(ResponseMessage response);

    @Override
    public Vector<ChatMessage> getMessages() {
	// TODO Auto-generated method stub
	return null;
    }

}
