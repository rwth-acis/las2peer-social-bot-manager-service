package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

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
		managers.add(generator.generate(DialogueManagerType.AGENDA_TREE, messenger, frame));
	    }
	}

    }

    public DialogueAct handle(Intent semantic) {

	String intent = semantic.getKeyword();

	for (AbstractDialogueManager manager : this.managers) {
	    if (manager.hasIntent(intent)) {
		active = manager;
		DialogueAct response = manager.handle(semantic);
		return response;
	    }
	}

	return new DialogueAct(active.handleDefault());
    }

}
