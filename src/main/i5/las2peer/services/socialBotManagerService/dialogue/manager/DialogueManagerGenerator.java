package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.DefaultTaskOrientedManager;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.model.ChatResponse;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;
import i5.las2peer.services.socialBotManagerService.model.Messenger;

public class DialogueManagerGenerator {

    public AbstractDialogueManager generate(DialogueManagerType type, Messenger messenger) {
	return this.generate(type, messenger, null);
    }

    public AbstractDialogueManager generate(DialogueManagerType type, Messenger messenger, Frame frame) {

	System.out.println("generate Dialogue Manager " + type);

	AbstractDialogueManager manager;
	switch (type) {
	case NAIVE:
	    manager = generateNaiveDialogueManager(frame);
	    break;
	case SIMPLE:
	    manager = generateSimpleDialogueManager(messenger);
	    break;
	default:
	    manager = null;
	}

	if (frame != null)
	    manager.setStartIntent(frame.getIntent());

	return manager;
    }

    private AbstractDialogueManager generateNaiveDialogueManager(Frame frame) {

	DialogueGoal goal = new DialogueGoal(frame);
	DefaultTaskOrientedManager manager = new DefaultTaskOrientedManager(goal);
	manager.setStartIntent(frame.getIntent());

	return manager;
    }

    private AbstractDialogueManager generateSimpleDialogueManager(Messenger messenger) {

	SimpleDialogueManager manager = new SimpleDialogueManager();

	Collection<IncomingMessage> messages = messenger.getIncomingMessages();
	for (IncomingMessage message : messages)
	    if (message.getResponseArray() != null)
		for (ChatResponse response : message.getResponseArray())
		    manager.addIntent(message.getIntentKeyword(), response.getResponse());
	return manager;
    }

}
