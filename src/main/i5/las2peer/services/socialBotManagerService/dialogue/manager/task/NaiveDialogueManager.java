package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInputType;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.AbstractDialogueManager;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class NaiveDialogueManager extends AbstractDialogueManager {

    DialogueGoal goal;
    String start_intent;
    boolean optional;

    public NaiveDialogueManager(DialogueGoal goal) {
	this.goal = goal;
    }

    @Override
    public DialogueAct handle(Intent semantic) {

	assert semantic != null : "naive dm handle: semantic is null";
	assert semantic.getKeyword() != null : "naive dm handle: semantic has no intent";
	assert semantic.getIntentType() != null : "no intent type set";
	System.out.println(semantic.getIntentType());

	// first call
	String intent = semantic.getKeyword();
	if (intent.equals(start_intent)) {
	    // request next slot
	    goal.reset();
	    optional = false;
	    Slot nextSlot = goal.next();
	    DialogueAct act = goal.getRequestAct(nextSlot);
	    return act;
	}

	// get corresponding slot
	Slot slot = null;
	if (goal.contains(intent)) {
	    slot = goal.getSlot(intent);
	    if (slot == null) {
		System.out.println("naive dm handle: slot not found for intent: " + intent);
	    }
	}

	DialogueAct act = new DialogueAct();
	switch (semantic.getIntentType()) {
	case INFORM:

	    // fill slot
	    for (Entity entity : semantic.getEntities()) {
		String value = entity.getValue();
		if (slot.validate(value))
		    this.goal.fill(slot, value);
		else {
		    // request slot again
		    Slot nextSlot = goal.next();
		    act = goal.getRequestAct(nextSlot);
		    return act;
		}

	    }

	    // arrays
	    if (slot.getParameter().isArray()) {

		String name = slot.getName().replaceAll("_", " ").substring(0, slot.getName().length() - 1);
		act.setMessage("do you want to add another " + name);
		ExpectedInput input = new ExpectedInput();
		input.setIntend(slot.getConfirmIntent());
		input.setType(ExpectedInputType.Confirmation);
		act.setExpected(input);
		return act;

	    } else {

		// check if ready
		if (!optional && goal.isReady())
		    return goal.getReqConfAct();

	    }

	    // check if full
	    if (goal.isFull())
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

	    System.out.println(semantic.getKeyword());
	    if (semantic.getKeyword().contentEquals(goal.getFrame().getConfirmIntent())) {

		if (!goal.isFull()) {
		    act = goal.getReqOptionalAct();
		    this.optional = true;
		    return act;
		}

		// perform action
		act.setAction(goal.getServiceAction());
		act.setMessage("perform action");
		goal.reset();
		optional = false;

		return act;
	    }

	    if (semantic.getKeyword().contentEquals(goal.getFrame().getConfirmIntent().concat("_optional"))) {

		// request next slot
		nextSlot = goal.next();
		act = goal.getRequestAct(nextSlot);
		return act;
	    }

	    if (semantic.getKeyword().contentEquals(slot.getConfirmIntent())) {
		act = goal.getRequestAct(slot);
		return act;
	    }

	    break;

	case DENY:

	    // deny frame
	    if (semantic.getKeyword().contentEquals(goal.getFrame().getConfirmIntent())) {
		// reset
		goal.reset();
		optional = false;
		nextSlot = goal.next();
		act = goal.getRequestAct(nextSlot);
		return act;
	    }

	    // deny optional frame
	    if (semantic.getKeyword().contentEquals(goal.getFrame().getConfirmIntent().concat("_optional"))) {

		// perform action
		act.setAction(goal.getServiceAction());
		act.setMessage("perform action");
		goal.reset();
		optional = false;
		return act;
	    }

	    // deny array slot
	    if (semantic.getKeyword().contentEquals(slot.getConfirmIntent())) {

		// check if ready
		if (!optional && goal.isReady())
		    return goal.getReqConfAct();

		// request next slot
		nextSlot = goal.next();
		act = goal.getRequestAct(nextSlot);
		return act;
	    }

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

	return act;
    }

    @Override
    public boolean hasIntent(String intent) {
	if (intent.contentEquals(this.start_intent))
	    return true;
	if (intent.contentEquals(goal.getFrame().getConfirmIntent())
		|| intent.contentEquals(goal.getFrame().getConfirmIntent() + "_optional"))
	    return true;
	return goal.contains(intent);
    }

    @Override
    public DialogueAct handleDefault() {
	// request next slot
	Slot nextSlot = goal.next();
	DialogueAct act = goal.getRequestAct(nextSlot);
	return act;
    }

    public void setStartIntent(String intent) {
	this.start_intent = intent;
    }

}
