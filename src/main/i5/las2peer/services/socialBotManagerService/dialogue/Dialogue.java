package i5.las2peer.services.socialBotManagerService.dialogue;

import i5.las2peer.services.socialBotManagerService.dialogue.nlg.AbstractLanguageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.DefaultMessageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;

public class Dialogue {

    MetaDialogueManager manager;
    LanguageUnderstander nlu;
    AbstractLanguageGenerator nlg;
    DialogueAct lastAct;

    public Dialogue(Messenger messenger) {
	this.manager = new MetaDialogueManager(messenger);
    }

    public ResponseMessage handle(MessageInfo message) {

	Intent semantic = message.getIntent();

	if (lastAct != null && lastAct.hasExpected()) {

	    ExpectedInput input = lastAct.getExpected();
	    switch (input.getType()) {
	    case Confirmation:
		if (semantic.getType() == IntentType.CONFIRM || semantic.getType() == IntentType.DENY) {
		    String intent = input.getIntend();
		    semantic.setKeyword(intent);
		}
		break;
	    default:

		String intent = input.getIntend();
		semantic.setKeyword(intent);
		semantic.setIntentType(semantic.getType());
		Entity entity = new Entity(lastAct.getExpected().getEntity(), message.getMessage().getText());
		semantic.addEntity(lastAct.getExpected().getEntity(), entity);

		break;
	    }

	}

	DialogueAct act = manager.handle(semantic);
	if (act.isFull())
	    this.lastAct = null;
	else
	    this.lastAct = act;

	if (act.hasAction()) {
	    System.out.println("perform action " + act.getAction().getFunction().getServiceName() + " "
		    + act.getAction().getFunction().getFunctionName());

	    OpenAPIConnector.sendRequest(act.getAction());
	}

	System.out.println(act);

	ResponseMessage res = nlg.parse(act);
	if (res == null) {
	    DefaultMessageGenerator gen = new DefaultMessageGenerator();
	    res = gen.get(act);
	}

	if (res == null)
	    res = new ResponseMessage(act.getMessage());

	if (act.hasEnums()) {
	    for (String enu : act.getExpected().getEnums()) {
		res.addButton(enu);
	    }
	}

	return res;
    }

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

    public AbstractLanguageGenerator getNlg() {
	return nlg;
    }

    public void setNlg(AbstractLanguageGenerator nlg) {
	this.nlg = nlg;
    }

}
