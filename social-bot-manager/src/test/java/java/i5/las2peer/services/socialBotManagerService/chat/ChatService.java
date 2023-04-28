package i5.las2peer.services.socialBotManagerService.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This enum lists all available messenger services. The string value has to
 * match the sync meta model.
 */
public enum ChatService {

	@JsonProperty("Rocket.Chat")
	ROCKET_CHAT("Rocket.Chat", RocketChatMediator.class),

	@JsonProperty("Slack")
	SLACK("Slack", SlackChatMediator.class),

	@JsonProperty("Telegram")
	TELEGRAM("Telegram", TelegramChatMediator.class),

	@JsonProperty("Moodle Chat")
	MOODLE_CHAT("Moodle Chat", MoodleChatMediator.class),

	@JsonProperty("Moodle Forum")
	MOODLE_FORUM("Moodle Forum", MoodleForumMediator.class),

	UNKNOWN("", null);

	/**
	 * The string representation of the messenger service used by the sync meta
	 * model.
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
	@Override
	public final String toString() {
		return this.string;
	}

	/**
	 * 
	 * @param string String to be compared to the representation
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
