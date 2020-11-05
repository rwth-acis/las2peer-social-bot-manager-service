package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.MetaDialogueManager;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.LanguageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.DefaultNlu;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;

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
     * @param manager
     */
    public DialogueHandler(Messenger messenger, MetaDialogueManager manager) {
	this.messenger = messenger;
	this.manager = manager;
	this.openDialogues = new HashMap<>();
	invariant();
    }

    /**
     * @param message
     * @param bot
     * @return
     */
    public ResponseMessage handleMessage(ChatMessage message, Bot bot) {

	assert message != null : "message parameter is null";
	assert message.getText() != null : "message has no text";
	assert message.getChannel() != null : "message has no channel";
	assert bot != null : "bot parameter is null";
	invariant();

	System.out.println("Handle message \"" + message.getText() + "\" in channel " + message.getChannel());
	if (message == null || message.getText() == null || message.getChannel() == null)
	    return null;

	Map<String, LanguageUnderstander> nlus = bot.getNLUs();
	Map<String, LanguageGenerator> nlgs = bot.getNLGs();

	Intent intent = null;

	// message command
	if (message.hasCommand()) {
	    System.out.println("treat command as intent: " + message.getCommand());
	    intent = new Intent(message.getCommand(), 1.0f);
	} else {

	    // NLU Module
	    try {
		System.out.println("Intent Extraction now with  : " + this.currentNluModel.get(message.getChannel()));
		intent = bot.getRasaServer(currentNluModel.get(message.getChannel())).getIntent(message.getText());
	    } catch (Exception e) {
		e.printStackTrace();

		// fallback default nlu
		LanguageUnderstander dnlu = new DefaultNlu();
		intent = dnlu.getIntent(message.getText());

	    }
	}

	String channel = message.getChannel();

	// Cancel dialogue, Start new dialogue
	if (intent.getIntentType() == IntentType.CANCEL || intent.getIntentType() == IntentType.START) {

	    if (this.openDialogues.containsKey(channel)) {
		this.openDialogues.remove(channel);
	    }
	}

	ResponseMessage response = null;

	Dialogue dialogue = null;

	// get dialogue
	if (this.openDialogues.containsKey(channel)) {
	    dialogue = this.openDialogues.get(channel);
	    System.out.println("resume open dialogue: " + message.getChannel());

	} else {
	    System.out.println("start new dialogue: " + message.getChannel());

	    dialogue = new Dialogue(messenger);
	    this.openDialogues.put(channel, dialogue);
	}

	// handle dialogue
	try {
	    response = this.manager.handle(dialogue);

	} catch (Exception e) {
	    e.printStackTrace();
	    response = new ResponseMessage("I am sorry. I had an error.");
	    this.openDialogues.remove(channel);
	}
	

	return handleResponse(response);
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
