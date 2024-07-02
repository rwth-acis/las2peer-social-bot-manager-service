package services.socialBotManagerService.chat;

import services.socialBotManagerService.model.IncomingMessage;

public abstract class StateContainer {

	public abstract IncomingMessage getIntentState(String channel, String user);
	
}
