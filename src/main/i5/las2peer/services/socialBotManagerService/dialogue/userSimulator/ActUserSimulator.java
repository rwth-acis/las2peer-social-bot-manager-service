package i5.las2peer.services.socialBotManagerService.dialogue.userSimulator;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public abstract class ActUserSimulator  {

	public abstract Intent handle(DialogueAct act);
	
}
