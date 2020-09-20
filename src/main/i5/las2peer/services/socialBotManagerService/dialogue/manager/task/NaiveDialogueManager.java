package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.AbstractDialogueManager;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class NaiveDialogueManager extends AbstractDialogueManager {

    DialogueGoal goal;
    String start_intent;

    public NaiveDialogueManager(DialogueGoal goal) {
	this.goal = goal;
    }

    @Override
    public DialogueAct handle(Intent semantic) {

	assert semantic != null : "naive dm handle: semantic is null";
	assert semantic.getKeyword() != null : "naive dm handle: semantic has no intent";
	
	
	// first call
	String intent = semantic.getKeyword();
	if (intent.equals(start_intent)) {
	    // request next slot
	    Slot nextSlot = goal.next();
	    DialogueAct act = goal.getRequestAct(nextSlot);
	    return act;
	}

	// get corresponding slot	
	Slot slot = goal.getSlot(intent);
	if (slot == null) {
	    System.out.println("naive dm handle: slot not found for intent: " + intent);
	}

	DialogueAct act = new DialogueAct();
	switch (semantic.getType()) {
	case INFORM:

	    // fill slot
	    for (Entity entity : semantic.getEntities()) {
		String value = entity.getValue();
		if (slot.validate(value))
		    this.goal.fill(slot, value);
	    }

	    // check if ready
	    if (goal.isReady())
		return goal.getReqConfAct();

	    // request next slot
	    Slot nextSlot = goal.next();
	    act = goal.getRequestAct(nextSlot);
	    return act;
	case REQUEST:

	    // inform about slot
	    act = goal.getInformAct(slot);
	    return act;
	case CONFIRM:

	    break;

	case DENY:

	    // delete slot value
	    goal.delete(slot);

	    // ask for new value
	    act = goal.getInformAct(slot);
	    break;

	default:
	    // request next slot
	    nextSlot = goal.next();
	    act = goal.getRequestAct(nextSlot);
	    return act;
	}

	return null;
    }

    @Override
    public boolean hasIntent(String intent) {
	if (intent.equals(this.start_intent))
	    return true;
	return goal.contains(intent);
    }

    @Override
    public DialogueAct handleDefault() {
	// TODO Auto-generated method stub
	return null;
    }

    public void setStartIntent(String intent) {
	this.start_intent = intent;
    }

}
