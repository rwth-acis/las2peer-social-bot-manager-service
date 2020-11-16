package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.MetaDialogueManager;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.PipelineManager;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.Messenger;

/**
 * The DialogueHandler maintains all open dialogues.
 */
public class DialogueHandler {

	/**
	 * The messenger this dialogue handler corresponds to.
	 */
	private Messenger messenger;

	/**
	 * The meta dialogue manager handles incoming messages in context of open
	 * dialogues.
	 */
	private MetaDialogueManager manager;

	/**
	 * This map contains all started dialogues. The key is the channel id.
	 */
	private Map<String, Dialogue> openDialogues;

	/**
	 * @param messenger
	 */
	public DialogueHandler(Messenger messenger) {

		assert messenger != null : "messenger is null";
		System.out.println("create dialogue handler");

		this.messenger = messenger;
		this.openDialogues = new HashMap<>();

		this.manager = new PipelineManager(messenger);

		invariant();
	}

	/**
	 * @param message
	 * @return
	 */
	public ResponseMessage handleMessage(ChatMessage message) {

		assert message != null : "message parameter is null";
		assert message.getText() != null : "message has no text";
		assert message.getChannel() != null : "message has no channel";
		invariant();

		System.out.println("Handle message \"" + message.getText() + "\" in channel " + message.getChannel());
		if (message == null || message.getText() == null || message.getChannel() == null)
			return null;

		// get dialogue
		Dialogue dialogue = null;
		String channel = message.getChannel();
		if (this.openDialogues.containsKey(channel)) {
			dialogue = this.openDialogues.get(channel);
			System.out.println("resume open dialogue: " + message.getChannel());

		} else {
			System.out.println("start new dialogue: " + message.getChannel());

			dialogue = new Dialogue(messenger);
			this.openDialogues.put(channel, dialogue);
		}

		// handle dialogue
		ResponseMessage response = null;
		try {

			response = this.manager.handle(messenger, message, dialogue);

		} catch (Error e) {
			System.out.println("error in dialogue management");
			response = handleError(channel);
			e.printStackTrace();
		}

		// null as error
		if (response == null)
			response = handleError(channel);

		// end dialogue
		if (response.isEnd())
			this.openDialogues.remove(channel);

		return handleResponse(response);
	}

	private ResponseMessage handleError(String channel) {
		System.out.println("handle error in channel: " + channel);
		ResponseMessage response = new ResponseMessage("I am sorry. I had an error. Restart conversation.", channel);
		this.openDialogues.remove(channel);
		return response;
	}

	private ResponseMessage handleResponse(ResponseMessage response) {

		assert response != null : "response parameter is null";
		assert response.getChannel() != null : "response has no channel";
		invariant();

		String channel = response.getChannel();
		if (response.isEnd() && openDialogues.containsKey(channel))
			this.openDialogues.remove(channel);

		return response;

	}

	public MetaDialogueManager getManager() {
		return manager;
	}

	public void setManager(MetaDialogueManager manager) {
		this.manager = manager;
	}

	public void invariant() {
		assert this.manager != null : "no meta dialogue manager set";
		assert this.openDialogues != null : "open dialogue map not initilized";
		assert this.messenger != null : "dialogue handler has no related messenger";
	}

}
