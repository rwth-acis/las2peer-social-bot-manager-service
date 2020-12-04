package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Messenger;

public abstract class MetaDialogueManager {

	Collection<AbstractDialogueManager> managers;
	AbstractDialogueManager active;

	public MetaDialogueManager() {

	}

	public abstract ResponseMessage handle(Messenger messenger, ChatMessage message, Dialogue dialogue);

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

}
