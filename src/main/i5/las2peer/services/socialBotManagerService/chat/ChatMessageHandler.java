package i5.las2peer.services.socialBotManagerService.chat;

public abstract class ChatMessageHandler {

	/**
	 * Template of a get function.
	 * 
	 * @param message The message in the format the chat service uses, stored in a String.
	 */
	abstract void handleMessage(String message);
}
