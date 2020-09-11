package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class MetaDialogueManager {

    Collection<AbstractDialogueManager> managers;
    AbstractDialogueManager active;

    public MetaDialogueManager(Messenger messenger) {

	DialogueManagerGenerator generator = new DialogueManagerGenerator();
	if (messenger.getIncomingMessages() != null)
	    managers.add(generator.generate(DialogueManagerType.SIMPLE, messenger));
	if (messenger.getFrames() != null)
	    managers.add(generator.generate(DialogueManagerType.AGENDA_TREE, messenger));

    }

    public String handle(Intent semantic) {

	String intent = semantic.getKeyword();

	for (AbstractDialogueManager manager : this.managers) {
	    if (manager.hasIntent(intent)) {
		active = manager;
		return manager.handle(semantic);
	    }
	}

	return active.handleDefault();
    }

}
