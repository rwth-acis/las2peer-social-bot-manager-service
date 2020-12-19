package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.DefaultTaskOrientedManager;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.model.ChatResponse;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.model.MessengerElement;
import i5.las2peer.services.socialBotManagerService.model.Selection;

public class DialogueManagerGenerator {

	public AbstractDialogueManager generate(MessengerElement element) {
		assert element != null;
		AbstractDialogueManager res = null;
		
		if (element instanceof Frame) 
			res = generateTaskOrientedRule((Frame) element);
		
		if (element instanceof IncomingMessage) {
			ArrayList<IncomingMessage> messages = new ArrayList<>();
			messages.add((IncomingMessage) element);
			res = generateSimpleMessages(messages);			
		}
		
		if (element instanceof Selection) 
			res = generateSimpleSelection((Selection) element);

		return res;
		
	}

	public AbstractDialogueManager generate(DialogueManagerType type, Messenger messenger, Frame frame,
			Selection selection) {

		System.out.println("generate Dialogue Manager " + type);

		AbstractDialogueManager manager;
		switch (type) {
		case TASK_ORIENTED_RULE:
			manager = generateTaskOrientedRule(frame);
			break;
		case SIMPLE_MESSAGES:
			manager = generateSimpleMessages(messenger.getIncomingMessages());
			break;
		case SIMPLE_SELECTION:
			manager = generateSimpleSelection(selection);
			break;
		default:
			manager = null;
		}

		if (frame != null)
			manager.setStartIntent(frame.getIntent());

		return manager;
	}

	public AbstractDialogueManager generateTaskOrientedRule(Frame frame) {

		DialogueGoal goal = new DialogueGoal(frame);
		DefaultTaskOrientedManager manager = new DefaultTaskOrientedManager(goal);
		manager.setStartIntent(frame.getIntent());

		return manager;
	}

	public AbstractDialogueManager generateSimpleMessages(Collection<IncomingMessage> messages) {

		MultiMessageDialogueManager manager = new MultiMessageDialogueManager();

		for (IncomingMessage message : messages)
			if (message.getResponseArray() != null)
				for (ChatResponse response : message.getResponseArray())
					manager.addIntent(message.getIntentKeyword(), response.getResponse());
		return manager;
	}

	public AbstractDialogueManager generateSimpleSelection(Selection selection) {

		SimpleSelectionManager manager = new SimpleSelectionManager(selection);
		manager.init(selection);
		return manager;

	}

}
