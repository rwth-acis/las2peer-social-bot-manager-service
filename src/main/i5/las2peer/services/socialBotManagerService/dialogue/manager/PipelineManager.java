package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.Command;
import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.DefaultMessageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.EnglishMessageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.GermanMessageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.Language;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.LanguageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.MessageFile;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.notification.EventToMessageTrigger;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.model.ServiceEvent;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.FallbackNlu;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;

/**
 * Meta Dialogue Manager that uses different modules for language understanding,
 * dialogue management and language generation
 */
public class PipelineManager extends MetaDialogueManager {

	Messenger messenger;

	public PipelineManager() {
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
		assert info != null;

		// Management
		DialogueAct act = null;
		if (message.hasCommand())
			act = handleCommandManagement(info, dialogue, messenger);
		else
			act = handleManagement(info, dialogue, messenger);
		assert act != null : "act is null";
		System.out.println(act);

		// Generation
		Map<String, LanguageGenerator> nlgs = messenger.getBot().getNLGs();
		ResponseMessage res = handleGeneration(act, dialogue, message, nlgs);
		assert res != null : "res is null";
		res.setChannel(message.getChannel());
		System.out.println(res);

		return res;
	}

	protected MessageInfo handleCommandUnderstanding(ChatMessage message) {

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

	protected DialogueAct handleCommandManagement(MessageInfo message, Dialogue dialogue, Messenger messenger) {

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
				return gen.getMainMenuAct(messenger.getBot(), operations);
			case START:
				gen = new DialogueActGenerator();
				return gen.getMainMenuAct(messenger.getBot(), messenger.getCommands());
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
			return gen.getMainMenuAct(messenger.getBot(), operations);
		}

		// not recognized command
		return gen.getUnknownCommandAct();

	}

	protected MessageInfo handleUnderstanding(ChatMessage message, Map<String, LanguageUnderstander> nlus) {

		assert message != null : "message is null";
		assert message.getChannel() != null : "message has no channel";
		assert message.getText() != null : "message has no text";
		assert nlus != null : "nlus is null";

		// try registered nlu modules
		Intent intent = null;
		List<LanguageUnderstander> nluList = new ArrayList<>(nlus.values());
		int i = 0;
		while (intent == null && i < nluList.size()) {
			try {
				System.out.println("Intent Extraction: " + message.getChannel());
				intent = nluList.get(i).parse(message);

			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}
		
		// use fallback nlu
		if (intent == null) {
			LanguageUnderstander fallbackNLU = new FallbackNlu();
			System.out.println("Intent extraction with default nlu: " + message.getChannel());
			intent = fallbackNLU.parse(message);
		}

		if (intent != null)
			intent.setIntentType(intent.deriveType());

		// return message info
		MessageInfo res = new MessageInfo();
		res.setIntent(intent);
		res.setMessage(message);

		return res;
	}

	protected DialogueAct handleDirectInput(MessageInfo message, Dialogue dialogue) {

		assert dialogue != null;
		assert message != null;
		assert message.getMessage() != null;

		String text = message.getMessage().getText();

		if (!dialogue.hasExpected())
			return null;

		ExpectedInput expected = dialogue.getExpected();
		InputType expectedType = expected.getType();
		System.out.println("dialogue has exptected input: " + expectedType);

		if (message.getIntent() == null)
			message.setIntent(new Intent("", 0));

		Intent semantic = message.getIntent();
		IntentType intentType = semantic.getIntentType();

		if (expectedType == InputType.Confirmation) {

			System.out.println("IntentType: " + intentType);
			if (intentType == IntentType.CONFIRM || intentType == IntentType.DENY) {
				semantic.setKeyword(expected.getIntend());
				semantic.setIntentType(intentType);
				message.setIntent(semantic);

				if (dialogue.getActiveManager() != null)
					return dialogue.handle(dialogue.getActiveManager(), message);

			} else
				return DialogueActGenerator.getInvalidValueAct(expected);

		} else if (expected.validate(semantic, text))

		{
			System.out.println("expected input is valid");
			semantic.setKeyword(expected.getIntend());
			semantic.setIntentType(semantic.deriveType());
			Entity entity = new Entity(expected.getEntity(), text);
			semantic.addEntity(expected.getEntity(), entity);
			message.setIntent(semantic);

			if (dialogue.getActiveManager() != null)
				return dialogue.handle(dialogue.getActiveManager(), message);

		} else
			return DialogueActGenerator.getInvalidValueAct(expected);

		return null;
	}

	protected DialogueAct handleManagement(MessageInfo message, Dialogue dialogue, Messenger messenger) {

		assert dialogue != null : "dialogue is null";
		assert message != null : "message is null";

		// handle direct input
		if (dialogue.hasExpected()) {
			DialogueAct res = handleDirectInput(message, dialogue);
			if (res != null)
				return res;
		}

		// handle invalid messageInfo intent
		if (message.getIntent() == null)
			return DialogueActGenerator.getNLUErrorAct();

		assert message.getIntent() != null : "message has no intent";
		assert message.getIntent().getKeyword() != null : "no intent keyword";

		Intent semantic = message.getIntent();
		String intent = semantic.getKeyword();
		if (semantic.getIntentType() == null)
			semantic.setIntentType(semantic.deriveType());

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
		assert messenger != null : "messenger is null";
		DialogueActGenerator gen = new DialogueActGenerator();
		List<Command> operations = messenger.getCommands();
		dialogue.setActiveManager(null);
		if (operations == null)
			operations = new ArrayList<>();
		return gen.getMainMenuAct(messenger.getBot(), operations);

	}

	protected String handleAction(OpenAPIAction action, ChatMessage message, Dialogue dialogue) {

		assert action != null : "action is null";
		assert action.getFunction() != null : "openapi action has no service function";

		String eventId = handleEvents(message, dialogue);
		if (eventId != null)
			action.addQueryParameter("botEventId", eventId);

		System.out.println("perform action " + action.getFunction().getServiceName() + " "
				+ action.getFunction().getFunctionName());

		String response = null;
		response = OpenAPIConnector.sendRequest(action);

		return response;

	}

	protected String handleEvents(ChatMessage message, Dialogue dialogue) {

		assert messenger != null;
		assert dialogue != null;

		if (!dialogue.hasActiveFrame())
			return null;

		Frame frame = dialogue.getActiveFrame();
		if (!frame.hasServiceEvents())
			return null;

		System.out.println("activate events " + frame.getServiceEvents().size());
		Bot bot = messenger.getBot();
		ChatMediator mediator = messenger.getChatMediator();

		String eventId = UUID.randomUUID().toString();
		Collection<ServiceEvent> triggers = frame.getServiceEvents();
		for (ServiceEvent event : triggers) {
			String response = event.getResponse().getResponse();
			String channel = message.getChannel();
			ResponseMessage responseMessage = new ResponseMessage(response, channel);
			EventToMessageTrigger trigger = new EventToMessageTrigger(eventId, event, responseMessage, mediator);
			bot.addActiveTrigger(trigger);

			System.out.println("activate event trigger " + event.getName() + " on bot " + bot.getName());
		}
		return eventId;

	}

	protected ResponseMessage handleGeneration(DialogueAct act, Dialogue dialogue, ChatMessage message,
			Map<String, LanguageGenerator> nlgs) {

		assert act != null : "dialogue act parameter is null";
		invariant();

		ResponseMessage res = null;
		MessageFile file = null;

		// act includes action
		if (act.hasAction()) {
			OpenAPIAction action = act.getAction();
			String response = handleAction(action, message, dialogue);
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
		if (res == null && act.getIntentType() != null) {
			DefaultMessageGenerator gen = null;
			if (messenger.getBot() == null || messenger.getBot().getLanguage() == Language.ENGLISH)
				gen = new EnglishMessageGenerator();
			else
				gen = new GermanMessageGenerator();

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
