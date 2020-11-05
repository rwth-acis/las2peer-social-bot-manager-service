package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;

public class MetaDialogueManager {

    Collection<AbstractDialogueManager> managers;
    AbstractDialogueManager active;

    public MetaDialogueManager(Messenger messenger) {

	this.managers = new ArrayList<AbstractDialogueManager>();
	DialogueManagerGenerator generator = new DialogueManagerGenerator();
	if (messenger.getIncomingMessages() != null && !messenger.getIncomingMessages().isEmpty())
	    managers.add(generator.generate(DialogueManagerType.SIMPLE, messenger));
	if (messenger.getFrames() != null && !messenger.getFrames().isEmpty()) {
	    for (Frame frame : messenger.getFrames()) {
		managers.add(generator.generate(DialogueManagerType.NAIVE, messenger, frame));
	    }
	}

    }

    public DialogueAct handle(Intent semantic) {

	assert semantic != null : "semantic is null";
	assert managers != null : "managers is null";
	assert !managers.isEmpty() : "no dialogue manager defined";
	assert semantic.getKeyword() != null : "semantic has no intent";

	String intent = semantic.getKeyword();

	for (AbstractDialogueManager manager : this.managers) {
	    System.out.println("handle intent: " + intent);
	    if (manager.hasIntent(intent)) {
		active = manager;
		DialogueAct response = manager.handle(semantic);
		if (response.isFull()) {
		    active = null;
		}
		return response;
	    }
	}

	if (active != null) {
	    if (semantic.getType() == IntentType.CONFIRM || semantic.getType() == IntentType.DENY)
		active.handle(semantic);
	    return active.handleDefault();

	}

	return null;
    }

    public void reset() {

	for (AbstractDialogueManager manager : this.managers) {
	    manager.reset();
	}
    }

    public void resetActive() {
	this.active = null;
    }

    public ResponseMessage handle(Dialogue dialogue) {
	// TODO Auto-generated method stub
	return null;
    }

}
