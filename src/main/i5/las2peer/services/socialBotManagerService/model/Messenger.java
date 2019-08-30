package i5.las2peer.services.socialBotManagerService.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.websocket.DeploymentException;

import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.chat.SlackChatMediator;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;

public class Messenger {
	private String name;

	private ChatMediator chatMediator;
	private RasaNlu rasa;
	private HashMap<String, IncomingMessage> knownIntents;

	// Used for keeping conversation state per channel
	private HashMap<String, IncomingMessage> stateMap;


	public Messenger(String id, String chatService, String token, String rasaUrl)
			throws IOException, DeploymentException, ParseBotException
	{
		if (chatService.contentEquals("Slack")) {
			this.chatMediator = new SlackChatMediator(token);
		} else { // TODO: Implement more backends
			throw new ParseBotException("Umimplemented chat service: " + chatService);
		}
		this.name = id;

		this.rasa = new RasaNlu(rasaUrl);

		this.knownIntents = new HashMap<String, IncomingMessage>();
		this.stateMap = new HashMap<String, IncomingMessage>();
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

	public void handleMessages() {
		ArrayList<ChatMessage> newMessages = this.chatMediator.getMessages();

		for (ChatMessage message: newMessages) {
			Intent intent = this.rasa.getIntent(message.getText());
			if (intent == null) {
				continue;
			}


			IncomingMessage state = this.stateMap.get(message.getChannel());
			// No conversation state present, starting from scratch
			if (state == null) {
				// TODO: Tweak this
				if (intent.getConfidence() >= 0.1f) {
					state = this.knownIntents.get(intent.getKeyword());
				}
			}

			// TODO: Remove hard-coded intent, just for demonstration purposes
			if (intent.getKeyword().contentEquals("greet")) {
				this.chatMediator.sendMessageToChannel(message.getChannel(), "Hi!");
			}

			// No matching intent found, perform default action
			if (state == null) {
				state = this.knownIntents.get("default");
				if (state != null) {
					this.chatMediator.sendMessageToChannel(message.getChannel(), "This is the default intent handler. Intent was: " + intent.getKeyword());
				}
			}

			if (state != null) {
				// TODO: Get Response to give, optionally insert entity
				// Conversation flow is terminated, reset state
				if (state.getFollowingMessages().isEmpty()) {
					this.stateMap.remove(message.getChannel());
				}
			}
		}
	}
}
