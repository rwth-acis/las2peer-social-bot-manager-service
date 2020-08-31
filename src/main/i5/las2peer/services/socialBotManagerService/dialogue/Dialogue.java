package i5.las2peer.services.socialBotManagerService.dialogue;

import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;

public class Dialogue {

    AbstractDialogueManager manager;
    LanguageUnderstander nlu;
    LanguageGenerator nlg;
    
    public Dialogue(AbstractDialogueManager dialogueManager) {
	this.manager = dialogueManager;
    }
    
    public Dialogue(AbstractDialogueManager manager, LanguageUnderstander nlu, LanguageGenerator nlg) {

	this.manager = manager;
	this.nlu = nlu;
	this.nlg = nlg;
    }


    public String handle(MessageInfo message) {

	//Intent inputSemantic = nlu.getIntent(message);
	String outputSemantic = manager.handle(message.getIntent());
	// String response = nlg.translate(outputSemantic);
	return outputSemantic;
    }
    

    //public String handle(String message) {

//	Intent inputSemantic = nlu.getIntent(message);
	//String outputSemantic = manager.handle(inputSemantic);
	// String response = nlg.translate(outputSemantic);
//	return outputSemantic;
  //  }

    public AbstractDialogueManager getManager() {
	return manager;
    }

    public void setManager(AbstractDialogueManager manager) {
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
