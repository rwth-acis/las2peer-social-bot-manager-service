package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.AbstractDialogueManager;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.model.Frame;

public abstract class TaskOrientedManager extends AbstractDialogueManager {

	public abstract DialogueGoal getDialogueGoal();
	
	public Frame getFrame() {
		return this.getDialogueGoal().getFrame();
	}
		
}
