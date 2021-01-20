package i5.las2peer.services.socialBotManagerService.chat;

import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;

public abstract class StateContainer {

	public abstract IncomingMessage getIntentState(String channel, String user);
	
}
