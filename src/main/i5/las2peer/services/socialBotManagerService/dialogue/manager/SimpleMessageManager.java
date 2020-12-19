package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActGenerator;
import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;
import i5.las2peer.services.socialBotManagerService.model.MessengerElement;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class SimpleMessageManager extends AbstractDialogueManager {

	IncomingMessage message;
	Collection<AbstractDialogueManager> managers;

	public SimpleMessageManager(IncomingMessage message) {
		this.setStartIntent(message.getIntentKeyword());
		this.message = message;
		this.managers = new ArrayList<>();
		for (MessengerElement element : message.getFollowingMessages().values()) {
			DialogueManagerGenerator gen = new DialogueManagerGenerator();
			AbstractDialogueManager manager = gen.generate(element);
			this.managers.add(manager);
		}
	}

	@Override
	public DialogueAct handle(Intent intent) {
		assert intent != null;
		assert intent.getKeyword() != null;
		
		// first call
		String key = intent.getKeyword();
		if (key.contentEquals(message.getIntentKeyword())) {
			DialogueAct act = DialogueActGenerator.getAct(message.getIntentKeyword() + "_response");
			act.setMessage(message.getResponseMessage());
			return act;
		}
		
		//follow up
		for(AbstractDialogueManager manager :this.managers) {
			if(manager.getNLUIntents().contains(key))
				return manager.handle(intent);
		}
		
		return null;
		
	}

	@Override
	public Collection<String> getNLUIntents() {
		return message.getNLUIntents();
	}

	@Override
	public Collection<String> getNLGIntents() {
		return message.getNLGIntents();
	}

	@Override
	public void reset() {
		return;
	}

}
