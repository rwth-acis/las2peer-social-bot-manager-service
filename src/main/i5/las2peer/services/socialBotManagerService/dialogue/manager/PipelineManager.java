package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.Command;
import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.DefaultMessageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.LanguageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.MessageFile;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;

public class PipelineManager extends MetaDialogueManager {

	Messenger messenger;

	public PipelineManager(Messenger messenger) {
		super();
	}

	@Override
	public ResponseMessage handle(Messenger messenger, ChatMessage message, Dialogue dialogue) {

		assert message != null : "message is null";
		assert dialogue != null : "dialogue is null";

		assert messenger != null : "messenger is null";
		assert messenger.getBot() != null : "messenger has no bot";
		assert messenger.getBot().getNLUs() != null : "messenger bot has no NLU modules";

		if (this.messenger == null)
			this.messenger = messenger;

		// Understanding
		Map<String, LanguageUnderstander> nlus = messenger.getBot().getNLUs();
		MessageInfo info = null;
		if (message.hasCommand())
			info = handleCommandUnderstanding(message);
		else
			info = handleUnderstanding(message, nlus);
		assert info != null : "info is null";

		// Management
		DialogueAct act = null;
		if (message.hasCommand())
			act = handleCommandManagement(info, dialogue, messenger);
		if (act == null)
			act = handleManagement(info, dialogue, messenger);
		assert act != null : "act is null";

		// Generation
		Map<String, LanguageGenerator> nlgs = messenger.getBot().getNLGs();
		ResponseMessage res = handleGeneration(act, nlgs);
		assert res != null : "res is null";
		res.setChannel(message.getChannel());

		return res;
	}

	public MessageInfo handleCommandUnderstanding(ChatMessage message) {

		assert message != null : "message is null";
		assert message.getChannel() != null : "message has no channel";
		assert message.getCommand() != null : "message has no command";

		Intent intent = null;
		if (message.hasCommand()) {
			System.out.println("command detected: " + message.getCommand());
			intent = new Intent(message.getCommand(), 1.0f);
		}

		MessageInfo res = new MessageInfo();
		res.setIntent(intent);
		res.setMessage(message);

		return res;
	}

	public DialogueAct handleCommandManagement(MessageInfo message, Dialogue dialogue, Messenger messenger) {

		assert message != null : "message is null";
		assert message.getIntent() != null : "message has no intent";
		assert message.getIntent().getKeyword() != null : "no intent keyword";
		assert dialogue != null : "dialogue is null";

		Intent semantic = message.getIntent();
		String intent = semantic.getKeyword();

		semantic.setIntentType(semantic.deriveType());

		if (dialogue.getActiveManager() != null) {
			// build-in command intents
			switch (semantic.getIntentType()) {

			case REVERT:
				dialogue.revert();
				if (dialogue.getLastAct() != null)
					return dialogue.getLastAct();
			case CANCEL:
				dialogue.cancel();
				// return main menu
				DialogueActGenerator gen = new DialogueActGenerator();
				List<Command> operations = messenger.getCommands();
				return gen.getMainMenuAct(operations);
			case START:
				gen = new DialogueActGenerator();
				return gen.getMainMenuAct(messenger.getCommands());
			default:
				break;
			}
		}

		// priority intents (e.g start new frame)
		for (AbstractDialogueManager manager : dialogue.getManagers()) {
			if (manager.getStartIntent() != null && manager.getStartIntent().equalsIgnoreCase(intent)) {
				dialogue.setActiveManager(manager);
				return dialogue.handle(manager, message);
			}
		}

		DialogueActGenerator gen = new DialogueActGenerator();
		if (dialogue.isEmpty()) {

			// return main menu
			List<Command> operations = messenger.getCommands();
			return gen.getMainMenuAct(operations);
		}

		// not recognized command
		return gen.getUnknownCommandAct();

	}

	public MessageInfo handleUnderstanding(ChatMessage message, Map<String, LanguageUnderstander> nlus) {

		assert message != null : "message is null";
		assert message.getChannel() != null : "message has no channel";
		assert message.getText() != null : "message has no text";
		assert nlus != null : "nlus is null";

		Intent intent = null;
		List<LanguageUnderstander> nluList = new ArrayList<>(nlus.values());
		int i = 0;
		while (intent == null && i < nluList.size()) {

			try {

				System.out.println("Intent Extraction now with  : " + message.getChannel());
				intent = nluList.get(i).parse(message);

			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}

		if (intent != null)
			intent.setIntentType(intent.deriveType());

		MessageInfo res = new MessageInfo();
		res.setIntent(intent);
		res.setMessage(message);

		return res;
	}

	public DialogueAct handleManagement(MessageInfo message, Dialogue dialogue, Messenger messenger) {

		assert message != null : "message is null";
		assert message.getIntent() != null : "message has no intent";
		assert message.getIntent().getKeyword() != null : "no intent keyword";
		assert dialogue != null : "dialogue is null";

		Intent semantic = message.getIntent();
		String intent = semantic.getKeyword();

		semantic.setIntentType(semantic.deriveType());
		if (semantic.getIntentType() == null)
			semantic.setIntentType(IntentType.UNKOWN);

		// handle direct input
		if (dialogue.hasExpected()) {
			ExpectedInput expected = dialogue.getExpected();
			InputType expectedType = expected.getType();
			IntentType intentType = semantic.getIntentType();
			String text = message.getMessage().getText();
			System.out.println("dialogue has exptected input: " + expected.getType());

			if (expectedType == InputType.Confirmation) {

				System.out.println("IntentType: " + intentType);
				if (intentType == IntentType.CONFIRM || intentType == IntentType.DENY) {
					semantic.setKeyword(expected.getIntend());
					semantic.setIntentType(intentType);
					message.setIntent(semantic);

				} else {

					DialogueActGenerator gen = new DialogueActGenerator();
					DialogueAct act = gen.getInvalidValueAct(expected);
					return act;

				}

			} else if (expected.validate(semantic, text)) {

				System.out.println("expected input is valid");
				semantic.setKeyword(expected.getIntend());
				semantic.setIntentType(semantic.deriveType());
				Entity entity = new Entity(expected.getEntity(), text);
				semantic.addEntity(expected.getEntity(), entity);
				message.setIntent(semantic);

				if (dialogue.getActiveManager() != null) {
					DialogueAct res = dialogue.handle(dialogue.getActiveManager(), message);
					if (res != null)
						return res;
				}

			} else {

				DialogueActGenerator gen = new DialogueActGenerator();
				DialogueAct act = gen.getInvalidValueAct(expected);
				return act;
			}
		}

		// use the active manager if it knows the intent
		if (dialogue.getActiveManager() != null) {
			AbstractDialogueManager activeManager = dialogue.getActiveManager();
			if (activeManager.hasIntent(intent))
				return dialogue.handle(activeManager, message);
		}

		// find new manager that knows the intent
		for (AbstractDialogueManager manager : dialogue.getManagers()) {
			if (manager.hasIntent(intent)) {
				dialogue.setActiveManager(manager);
				DialogueAct res = dialogue.handle(manager, message);
				assert res != null : "dialogue act is null";
				return res;
			}
		}

		// continue active manager
		if (dialogue.getActiveManager() != null) {
			DialogueAct res = dialogue.handle(dialogue.getActiveManager(), message);
			if (res != null)
				return res;
		}

		// return main menu
		System.out.println("return main menu");
		assert messenger != null : "messenger is null";
		DialogueActGenerator gen = new DialogueActGenerator();
		List<Command> operations = messenger.getCommands();
		dialogue.setActiveManager(null);
		if (operations == null)
			operations = new ArrayList<>();
		return gen.getMainMenuAct(operations);

	}

	public String handleAction(Messenger messenger, OpenAPIAction action) {

		assert action != null : "action is null";
		assert action.getFunction() != null : "openapi action has no service function";

		System.out.println("perform action " + action.getFunction().getServiceName() + " "
				+ action.getFunction().getFunctionName());

		String response = null;
		//if (action.getFunction().getActionType() == ActionType.OPENAPI)
			response = OpenAPIConnector.sendRequest(action);

		//if (action.getFunction().getActionType() == ActionType.SERVICE)
		//	response = OpenAPIConnector.sendSignedRequest(messenger.getBot(), action);

		return response;

	}

	public ResponseMessage handleGeneration(DialogueAct act, Map<String, LanguageGenerator> nlgs) {

		assert act != null : "dialogue act parameter is null";
		invariant();

		ResponseMessage res = null;
		MessageFile file = null;

		// act includes action
		if (act.hasAction()) {
			OpenAPIAction action = act.getAction();
			String response = handleAction(messenger, action);
			if (response != null) {
				System.out.println("response parsemode: " + action.getResponseParseMode());
				switch (action.getResponseParseMode()) {
				case FILE:
					file = new MessageFile();
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

		// nlg modules
		if (nlgs != null) {
			List<LanguageGenerator> nlgList = new ArrayList<>(nlgs.values());
			int i = 0;
			while (res == null && i < nlgList.size()) {

				if (act.hasIntent())
					res = nlgList.get(i).parse(act);
				i++;
			}
		}

		// default nlg
		if (act.hasIntent() && res == null) {
			DefaultMessageGenerator gen = new DefaultMessageGenerator();
			res = gen.parse(act);
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

		if (file != null)
			res.setFile(file);

		return res;

	}

	public void invariant() {

	}

}
