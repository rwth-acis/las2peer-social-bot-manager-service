package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.TaskOrientedManager;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.Messenger;

public abstract class MetaDialogueManager {

	Collection<AbstractDialogueManager> managers;
	AbstractDialogueManager active;

	public abstract ResponseMessage handle(Messenger messenger, ChatMessage message, Dialogue dialogue);

	public MetaDialogueManager() {

	}

	public void initialize(Messenger messenger) {
		assert messenger != null : "messenger is null";

		/*
		 * this.managers = new ArrayList<AbstractDialogueManager>();
		 * DialogueManagerGenerator generator = new DialogueManagerGenerator();
		 * 
		 * if (messenger.getIncomingMessages() != null &&
		 * !messenger.getIncomingMessages().isEmpty())
		 * managers.add(generator.generate(DialogueManagerType.SIMPLE_MESSAGES,
		 * messenger, null, null));
		 * 
		 * if (messenger.getSelections() != null &&
		 * !messenger.getSelections().isEmpty())
		 * 
		 * if (messenger.getFrames() != null && !messenger.getFrames().isEmpty()) { for
		 * (Frame frame : messenger.getFrames())
		 * managers.add(generator.generate(DialogueManagerType.TASK_ORIENTED_RULE,
		 * messenger, frame, null));
		 * 
		 * }
		 */

	}

	public Collection<TaskOrientedManager> getTaskOrientedManagers() {
		Collection<TaskOrientedManager> res = new ArrayList<TaskOrientedManager>();
		for (AbstractDialogueManager manager : this.managers) {
			if (manager instanceof TaskOrientedManager)
				res.add((TaskOrientedManager) manager);
		}
		return res;
	}

	public Collection<String> getNLUIntents() {
		Collection<String> res = new ArrayList<>();
		for (AbstractDialogueManager manager : this.managers) {
			if (manager.getNLUIntents() != null)
				res.addAll(manager.getNLUIntents());
		}
		return res;
	}

	public Collection<String> getNLGIntents() {
		Collection<String> res = new ArrayList<>();
		for (AbstractDialogueManager manager : this.managers) {
			if (manager.getNLGIntents() != null)
				res.addAll(manager.getNLGIntents());
		}
		return res;

	}

}
