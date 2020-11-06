package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.List;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.LanguageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.MessageFile;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;

public class PipelineManager {

    List<LanguageUnderstander> nlus;
    List<LanguageGenerator> nlgs;

    public ResponseMessage handle(ChatMessage message, Dialogue dialogue) {

	assert message != null : "message is null";
	assert dialogue != null : "dialogue is null";

	MessageInfo info = handleUnderstanding(message);
	assert info != null : "info is null";

	DialogueAct act = handleManagement(info, dialogue);
	assert act != null : "act is null";

	ResponseMessage res = handleGeneration(act);
	assert res != null : "res is null";

	return res;
    }

    public MessageInfo handleUnderstanding(ChatMessage message) {

	assert message != null : "message is null";
	assert message.getChannel() != null : "message has no channel";
	assert message.getText() != null : "message has no text";

	Intent intent = null;
	if (message.hasCommand()) {
	    System.out.println("treat command as intent: " + message.getCommand());
	    intent = new Intent(message.getCommand(), 1.0f);
	}
	
	int i = 0;
	while (intent == null && i < this.nlus.size()) {

	    try {

		System.out.println("Intent Extraction now with  : " + message.getChannel());
		intent = nlus.get(i).parse(message);

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    i++;
	}

	MessageInfo res = new MessageInfo();
	res.setIntent(intent);
	res.setMessage(message);

	return res;
    }

    public DialogueAct handleManagement(MessageInfo message, Dialogue dialogue) {

	assert message != null : "message is null";
	assert message.getIntent() != null : "message has no intent";
	assert message.getIntent().getKeyword() != null : "no intent keyword";
	assert dialogue != null : "dialogue is null";

	Intent semantic = message.getIntent();
	String intent = semantic.getKeyword();
	IntentType type = semantic.getIntentType();

	if (type == null)
	    type = IntentType.TALK;

	if (dialogue.isEmpty())
	    type = IntentType.START;

	// build-in command intents
	switch (type) {

	case REVERT:
	    dialogue.revert();
	    break;
	case CANCEL:
	case START:
	    dialogue.cancel();
	default:
	    dialogue.add(message);
	    break;
	}

	// priority intents
	for (AbstractDialogueManager manager : dialogue.getManagers()) {
	    if (manager.getStartIntent().contentEquals(intent)) {		
		return manager.handle(semantic);
	    }
	}
	
	// handle direct input
	ExpectedInput expected = dialogue.getExpected();
	InputType expectedType = expected.getType();
	IntentType intentType = semantic.getType();
	String text = message.getMessage().getText();

	if (expectedType == InputType.Confirmation) {

	    if (intentType == IntentType.CONFIRM || intentType == IntentType.DENY)
		semantic.setKeyword(expected.getIntend());

	} else if (!expected.validate(semantic, text)) {

	    DialogueActGenerator gen = new DialogueActGenerator();
	    DialogueAct act = gen.getInvalidValueAct(expected);
	    return act;
	}
	
	// use the active manager
	if (dialogue.getActiveManager() != null) {
	    AbstractDialogueManager activeManager = dialogue.getActiveManager();
	    if (activeManager.hasIntent(intent))
		return activeManager.handle(semantic);
	}
	
	// find new manager that knows the intent
	for (AbstractDialogueManager manager : dialogue.getManagers()) {
	    if (manager.hasIntent(intent)) {
		dialogue.setActiveManager(manager);
		return manager.handle(semantic);
	    }
	}

	return null;
    }

    public String handleAction(OpenAPIAction action) {

	assert action != null : "action is null";
	assert action.getFunction() != null : "openapi action has no service function";

	System.out.println("perform action " + action.getFunction().getServiceName() + " "
		+ action.getFunction().getFunctionName());

	String response = OpenAPIConnector.sendRequest(action);
	return response;

    }

    public ResponseMessage handleGeneration(DialogueAct act) {

	assert act != null : "dialogue act parameter is null";
	invariant();

	ResponseMessage res = null;

	if (act.hasAction()) {
	    OpenAPIAction action = act.getAction();
	    String response = handleAction(action);
	    if (response != null) {
		switch (action.getResponseParseMode()) {
		case FILE:
		    MessageFile file = new MessageFile();
		    file.setData(response);
		    file.setName(act.getFile());
		    break;
		case TEXT:
		    act.setMessage(response);
		    break;
		default:
		    break;
		}
	    }
	}


	int i = 0;
	while (res == null && i < nlgs.size()) {

	    if (act.hasIntent())
		res = nlgs.get(i).parse(act);
	    i++;
	}

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

	return res;

    }

    public void invariant() {
	assert this.nlus != null : "nlus is null";
	assert !this.nlus.isEmpty() : "nlus is empty";
	assert this.nlgs != null : "nlgs is null";
	assert !this.nlgs.isEmpty() : "nlgs is empty";
    }

}
