package i5.las2peer.services.socialBotManagerService.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

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

	// Key: intent keyword
	private HashMap<String, IncomingMessage> knownIntents;

	// Used for keeping conversation state per channel
	private HashMap<String, IncomingMessage> stateMap;

	private Random random;


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
		this.random = new Random();
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

	// Handles simple responses ("Chat Response") directly, logs all messages and
	// extracted intents into `messageInfos` for further processing later on.
	// TODO: This would be much nicer if we could get a las2peer context here, but this
	//       is usually called from the routine thread. Maybe a context can be shared across
	//       threads somehow?
	public void handleMessages(ArrayList<MessageInfo> messageInfos, Bot bot) {
		Vector<ChatMessage> newMessages = this.chatMediator.getMessages();

		for (ChatMessage message: newMessages) {
			Intent intent = null;

			// Special case: `!` commands
			if (message.getText().startsWith("!")) {
				// Split at first occurring whitespace
				String splitMessage[] = message.getText().split("\\s+", 2);

				// First word without '!' prefix
				String intentKeyword = splitMessage[0].substring(1);
				IncomingMessage incMsg = this.knownIntents.get(intentKeyword);
				// TODO: Log this? (`!` command with unknown intent / keyword)
				if (incMsg == null) {
					continue;
				}

				String entityKeyword = incMsg.getEntityKeyword();
				String entityValue = null;
				// Entity value is the rest of the message. The whole rest
				// is in the second element, since we only split it into two parts.
				if (splitMessage.length > 1) {
					entityValue = splitMessage[1];
				}

				intent = new Intent(intentKeyword, entityKeyword, entityValue);
			} else {
				intent = this.rasa.getIntent(message.getText());
			}

			String triggeredFunctionId = null;
			IncomingMessage state = this.stateMap.get(message.getChannel());

			// No conversation state present, starting from scratch
			if (state == null) {
				// TODO: Tweak this
				if (intent.getConfidence() >= 0.1f) {
					state = this.knownIntents.get(intent.getKeyword());
				}
			}

			// No matching intent found, perform default action
			if (state == null) {
				state = this.knownIntents.get("default");
			}

			if (state != null) {
				String response = state.getResponse(this.random);
				if (response != null) {
					this.chatMediator.sendMessageToChannel(message.getChannel(), response);
				}
				triggeredFunctionId = state.getTriggeredFunctionId();

				// If conversation flow is terminated, reset state
				if (state.getFollowingMessages().isEmpty()) {
					this.stateMap.remove(message.getChannel());
				}
			}

			messageInfos.add(new MessageInfo(message, intent,
					triggeredFunctionId, bot.getBotAgent(), bot.getName(),
					bot.getVle().getName()));
		}
	}
}
