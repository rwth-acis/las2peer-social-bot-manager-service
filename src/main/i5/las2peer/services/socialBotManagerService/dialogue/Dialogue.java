package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.MetaDialogueManager;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.AbstractLanguageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.DefaultMessageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.MessageFile;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.TableLanguageGenerator;
import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;

public class Dialogue {

    Messenger messenger;
    MetaDialogueManager manager;
    LanguageUnderstander nlu;
    AbstractLanguageGenerator nlg;
    DialogueAct lastAct;
    DefaultMessageGenerator gen;

    List<MessageInfo> inputs;

    public Dialogue(Messenger messenger) {
	this.messenger = messenger;
	this.manager = new MetaDialogueManager(messenger);
	this.nlg = new TableLanguageGenerator();
	this.gen = new DefaultMessageGenerator();
	this.inputs = new ArrayList<>();

    }

    public ResponseMessage handle(MessageInfo message) {

	Intent semantic = message.getIntent();

	if (semantic.getIntentType() != null
		&& (semantic.getIntentType() == IntentType.CANCEL || semantic.getIntentType() == IntentType.START)) {

	    manager.reset();
	    manager.resetActive();
	    System.out.println("cancel: input acts size: " + this.inputs.size());
	    this.lastAct = null;
	    this.inputs.clear();
	    DialogueActGenerator gen = new DialogueActGenerator();
	    return this.handle(gen.getMainMenuAct(messenger.getCommands()));
	}

	if (semantic.getIntentType() != null && semantic.getIntentType() == IntentType.REVERT && !inputs.isEmpty()) {

	    manager.reset();
	    this.inputs.remove(this.inputs.size() - 1);
	    this.lastAct = null;
	    for (MessageInfo msg : this.inputs) {
		System.out.println("redo " + msg.getIntent().getKeyword());
		this.handle(msg.getIntent(), msg.getMessage().getText());
	    }

	    semantic = this.inputs.get(this.inputs.size() - 1).getIntent();
	    this.lastAct = null;

	} else {
	    this.inputs.add(message);
	}

	DialogueAct act = this.handle(semantic, message.getMessage().getText());
	return this.handle(act);
    }

    public ResponseMessage handle(DialogueAct act) {

	ResponseMessage res = null;

	if (act.hasIntent())
	    res = nlg.parse(act);

	if (act.hasIntent() && res == null)
	    res = gen.parse(act);

	if (res == null)
	    res = new ResponseMessage(act.getMessage());

	if (act.hasEnums()) {
	    List<String> enumList = act.getExpected().getEnums();
	    for (String enu : enumList) {
		res.addButton(enu);
	    }
	}

	if (act.hasAction())
	    res.setEnd(true);

	if (act.getFile() != null) {
	    MessageFile file = new MessageFile();
	    file.setName(act.getFile());
	    file.setData(act.getMessage());
	    file.setType(".json");
	    res.setFile(file);
	}

	return res;

    }

    public DialogueAct handle(Intent semantic, String message) {

	System.out.println("last act: " + this.lastAct);
	if (this.lastAct != null && this.lastAct.hasExpected()) {

	    ExpectedInput input = this.lastAct.getExpected();

	    switch (input.getType()) {
	    case Confirmation:
		if (semantic.getType() == IntentType.CONFIRM || semantic.getType() == IntentType.DENY) {
		    String intent = input.getIntend();
		    semantic.setKeyword(intent);
		}
		break;
	    default:

		if (!input.validate(semantic, message)) {

		    DialogueAct act = new DialogueAct();
		    act.setExpected(input);
		    act.setMessage(gen.getInvalidValue(input));
		    return act;
		}

		String intent = input.getIntend();
		semantic.setKeyword(intent);
		semantic.setIntentType(semantic.getType());
		Entity entity = new Entity(this.lastAct.getExpected().getEntity(), message);
		semantic.addEntity(this.lastAct.getExpected().getEntity(), entity);

		break;
	    }

	}

	DialogueAct act = manager.handle(semantic);
	if (act == null) {
	    DialogueActGenerator gen = new DialogueActGenerator();
	    return gen.getMainMenuAct(messenger.getCommands());
	}

	if (act.isFull())
	    this.lastAct = null;
	else
	    this.lastAct = act;

	if (act.hasAction()) {
	    System.out.println("perform action " + act.getAction().getFunction().getServiceName() + " "
		    + act.getAction().getFunction().getFunctionName());

	    String response = OpenAPIConnector.sendRequest(act.getAction());
	    act.setMessage(response);

	}

	System.out.println(act);

	return act;
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
