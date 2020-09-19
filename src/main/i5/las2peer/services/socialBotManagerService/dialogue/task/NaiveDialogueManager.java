package i5.las2peer.services.socialBotManagerService.dialogue.task;

import i5.las2peer.services.socialBotManagerService.dialogue.AbstractDialogueManager;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;

public class NaiveDialogueManager extends AbstractDialogueManager {

    DialogueGoal goal;
    
    public NaiveDialogueManager(DialogueGoal goal) {
	this.goal = goal;
    }

    @Override
    public DialogueAct handle(Intent semantic) {
	
	assert semantic != null: "naive dm handle: semantic is null";
	assert semantic.getKeyword() != null: "naive dm handle: semantic has no intent";
	
	DialogueAct act = new DialogueAct();
	String intent = semantic.getKeyword();
	switch(semantic.getType()) {
	case INFORM:
	    Slot slot = goal.get(intent);
	    for(Entity entity: semantic.getEntities())
		goal.fill(slot, entity.getValue());    
	    
	    break;
	case REQUEST:
	    break;
	case CONFIRM:
	    break;
	case DENY:
	    slot = goal.get(intent);
	    
	    break;

	default:
	    break;
	
	}
	IntentType intent = semantic.getKeyword();
	goal.get(intent);
	
	return null;
    }
    
    @Override
    public boolean hasIntent(String intent) {
	return goal.contains(intent);
    }

    @Override
    public DialogueAct handleDefault() {
	// TODO Auto-generated method stub
	return null;
    }

}
