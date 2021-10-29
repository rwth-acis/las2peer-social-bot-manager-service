package i5.las2peer.services.socialBotManagerService.chat;

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
	 * @param parsedEvent JSON representation of incoming event message
	 */
	public abstract void handleEvent(JSONObject parsedEvent);

}
