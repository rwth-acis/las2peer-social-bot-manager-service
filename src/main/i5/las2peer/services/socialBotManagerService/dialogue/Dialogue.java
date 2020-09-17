package i5.las2peer.services.socialBotManagerService.dialogue;

import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;

public class Dialogue {

	MetaDialogueManager manager;
	LanguageUnderstander nlu;
	LanguageGenerator nlg;
	DialogueAct lastAct;

	public Dialogue(Messenger messenger) {
		this.manager = new MetaDialogueManager(messenger);
	}

	public String handle(MessageInfo message) {

		Intent semantic = message.getIntent();

		if (lastAct != null && lastAct.hasExpected()) {
			String intent = lastAct.getExpected().getIntend();
			semantic.setKeyword(intent);
			Entity entity = new Entity(lastAct.getExpected().getEntity(), message.getMessage().getText());
			semantic.addEntity(lastAct.getExpected().getEntity(), entity);
		}

		DialogueAct act = manager.handle(semantic);
		if (act.isFull())
			this.lastAct = null;
		else
			this.lastAct = act;

		System.out.println(act);
		// String response = nlg.translate(outputSemantic);
		return act.getMessage();
	}

	// public String handle(String message) {

//	Intent inputSemantic = nlu.getIntent(message);
	// String outputSemantic = manager.handle(inputSemantic);
	// String response = nlg.translate(outputSemantic);
//	return outputSemantic;
	// }

	public MetaDialogueManager getManager() {
		return manager;
	}

	public void setManager(MetaDialogueManager manager) {
		this.manager = manager;
	}

	public LanguageUnderstander getNlu() {
		return nlu;
	}

	public void setNlu(LanguageUnderstander nlu) {
		this.nlu = nlu;
	}

	public LanguageGenerator getNlg() {
		return nlg;
	}

	public void setNlg(LanguageGenerator nlg) {
		this.nlg = nlg;
	}

}
