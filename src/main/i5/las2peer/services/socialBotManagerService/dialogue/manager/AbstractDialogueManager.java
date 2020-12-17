package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public abstract class AbstractDialogueManager {

	private String startIntent;

	public abstract DialogueAct handle(Intent inputSemantic);

	public boolean hasIntent(String intent) {
		return getNLUIntents().contains(intent);
	}

	public boolean hasIntent(Intent intent) {
		return hasIntent(intent.getKeyword());
	}

	public abstract Collection<String> getNLUIntents();

	public abstract Collection<String> getNLGIntents();
	
	public abstract void reset();

	public String getStartIntent() {
		return startIntent;
	}

	public void setStartIntent(String startIntent) {
		this.startIntent = startIntent;
	}

}
