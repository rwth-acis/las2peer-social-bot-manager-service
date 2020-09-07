package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.model.ChatResponse;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class DialogueManagerGenerator {

    public AbstractDialogueManager generate(DialogueManagerType type, Messenger messenger) {

	AbstractDialogueManager manager;
	switch (type) {
	case AGENDA_TREE:
	    manager = generateAgendaDialogueManager(messenger);
	    break;
	default:
	    manager = null;
	}
	return manager;
    }

    private AbstractDialogueManager generateAgendaDialogueManager(Messenger messenger) {

	System.out.println("creating dialogue manager");
	AgendaDialogueManager manager = new AgendaDialogueManager();
	AgendaDialogueNode root = new AgendaDialogueNode();
	manager.setRoot(root);

	Collection<IncomingMessage> messages = messenger.getIncomingMessages();

	// Build Tree

	// Frames and Slots
	if (messenger.getFrames() != null) {
	    Collection<Frame> frames = messenger.getFrames();
	    for (Frame frame : frames) {
		AgendaDialogueNode node = new AgendaDialogueNode();
		node.setIntent(frame.getIntent());
		node.setPassive(false);
		node.addResponse(frame.getMessage());
		root.addChild(node);
		manager.setGoal(frame);
		manager.goalMessage = frame.getMessage();
		System.out.println("adding node: " + node.getIntent());
		for (Slot slot : frame.getSlots().values()) {
		    AgendaDialogueNode subNode = new AgendaDialogueNode();
		    subNode.setIntent(slot.getName());
		    subNode.setPassive(false);
		    subNode.setEntity(slot.getEntity());
		    subNode.addResponse(slot.getMessage());
		    node.addChild(subNode);
		    System.out.println("adding node: " + subNode.getIntent());
		}
	    }
	}

	// Incoming Messages and Chat Responses
	for (IncomingMessage message : messages) {
	    AgendaDialogueNode node = new AgendaDialogueNode();
	    node.setIntent(message.getIntentKeyword());
	    node.setPassive(true);
	    if(message.getResponseArray() != null) {
        	    for (ChatResponse response : message.getResponseArray()) {
        		node.addResponse(response.getResponse());
        	    }
	    }
	    root.addChild(node);
	    System.out.println("adding node: " + node.getIntent());
	}

	manager.validate();
	manager.reset();
	return manager;
    }
}
