package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.util.OptionalLong;
import java.util.Vector;

import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import net.minidev.json.JSONObject;

/**
 * An Event Chat Mediator handles messenger services that send push messages to
 * inform about incoming user activities (events).
 *
 */
public abstract class EventChatMediator extends ChatMediator {

	public EventChatMediator(String authToken) {
		super(authToken);
	}

	/**
	 * @param JSON representation of incoming event message
	 * @return
	 */
	public abstract ChatMessage handleEvent(JSONObject parsedEvent);

	@Override
	public abstract void sendMessageToChannel(ResponseMessage response);

	public boolean hasToken(String token) {
		return (this.authToken.equals(token));
	}
	
	@Override
	public Vector<ChatMessage> getMessages() {
		return null;
	}
	
	@Override
	public void sendFileMessageToChannel(String channel, File f, String text, OptionalLong id) {
		// TODO Auto-generated method stub
		
	}

}
