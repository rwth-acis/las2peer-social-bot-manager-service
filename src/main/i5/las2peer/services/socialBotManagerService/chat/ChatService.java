package i5.las2peer.services.socialBotManagerService.chat;

/**
 * This enum lists all available messenger services. Have to be extended when implementing a new
 * chat mediator. The string value has to match the sync meta model.
 */
public enum ChatService {
	
	ROCKET_CHAT ("Rocket.Chat", RocketChatMediator.class),
	
	SLACK_EVENT ("Slack", SlackEventChatMediator.class),
	
	SLACK_RTM ("Slack RTM", SlackRTMChatMediator.class),
	
	TELEGRAM ("Telegram", TelegramChatMediator.class),
	
	UNKNOWN ("", null);
	
	/**
	 * The string representation of the messenger service used by the sync meta model.
	 */
	public final String string;
	
	/**
	 * The class of the chat mediator that connects to the messenger service
	 */
	public final Class<? extends ChatMediator> mediatorClass;
	
	ChatService(String string, Class<? extends ChatMediator> mediatorClass) {
		this.string = string;
		this.mediatorClass = mediatorClass;
	}

	/**
	 * @return The string representation
	 */
	public final String toString() {
		return this.string;
	}
	
	/**
	 * @return The chat mediator subclass
	 */
	public final Class<? extends ChatMediator> getMediatorClass() {
		return this.mediatorClass;
	}
	
	/**
	 * 
	 * 
	 * @
	 * @return TRUE if string represents the chat service
	 */
	public final boolean isEquals(String string) {
		return (this.string.equals(string));
	}
	


	/**
	 * @param string representation of a chat service
	 * @return the enum representation of a chat service
	 */
	public static ChatService fromString(String string) {

		for (ChatService service : ChatService.values()) {
			if (string.equalsIgnoreCase(service.toString()))		
				return service;
		}
		return ChatService.UNKNOWN;
	}
	
}
