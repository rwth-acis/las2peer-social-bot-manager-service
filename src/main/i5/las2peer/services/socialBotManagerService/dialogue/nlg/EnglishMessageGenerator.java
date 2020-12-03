package i5.las2peer.services.socialBotManagerService.dialogue.nlg;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;

/**
 *	Generates default messages in English language
 *
 */
public class EnglishMessageGenerator extends DefaultMessageGenerator {
	
	@Override
	protected ResponseMessage getErrorSystem(DialogueAct act) {
		assert act != null;
		
		String message = "I am sorry. I had an error. Restart conversation.";
		ResponseMessage res = new ResponseMessage(message);
		return res;
	}

	@Override
	protected ResponseMessage getErrorNLU(DialogueAct act) {
		assert act != null;
		
		String message = "I am sorry. Currently my language understanding is not working. Please use explicit commands";
		ResponseMessage res = new ResponseMessage(message);
		return res;
	}

	@Override
	protected ResponseMessage getErrorCommandUnknown(DialogueAct act) {
		assert act != null;
		
		String message = "Sorry, I dont know this command.";
		ResponseMessage res = new ResponseMessage(message);
		return res;
	}

	@Override
	protected ResponseMessage getErrorInvalidInput(DialogueAct act) {
		assert act != null;
				
		String message = "This is not what i asked";
		switch(act.getExpected().getType()) {
		case Binary:
			break;
		case Confirmation:
			message = "please clearly state that you agree (yes) or disagree (no)";
			break;
		case Date:
			break;
		case Date_Time:
			break;
		case Decimal:
			break;
		case Email:
			break;
		case Enum:
			message = "Please choose one of this possible answers: \n";
			for (String enu : act.getExpected().getEnums()) {
				message = message + enu + "\n";
			}
			break;
		case Free:
			break;
		case Number:
			message = "Please answer with a number \n";
			break;
		case Url:
			message = "Please answer with a valid url \n";
			break;
		case Word:
			message = "Please answer in one word without spaces \n";
			break;
		default:
			break;		
		}
				
		ResponseMessage res = new ResponseMessage(message);
		return res;
	}
	
	@Override
	protected ResponseMessage getReqConfSlotProceed(DialogueAct act) {
		assert act != null;
				
		String name = act.getEntities().get("name");
		String message = "Do you want to add another *" + name + "*";
		ResponseMessage res = new ResponseMessage(message);
		return res;
	}

	// Frame Intents

	@Override
	protected ResponseMessage getReqConf(DialogueAct act) {
		assert act != null : "dialogue act parameter is null";

		String message = "We have all necessary data \n\n";
		for (Map.Entry<String, String> entry : act.getEntities().entrySet())
			message = message.concat(entry.getKey()).replaceAll("_", " ").concat(": \t ").concat(entry.getValue())
					.concat(" \n");
		message = message.concat("\nis this right? \n");

		ResponseMessage res = new ResponseMessage(message);
		return (res);
	}

	@Override
	protected ResponseMessage getReqOptional(DialogueAct act) {
		assert act != null : "dialogue act parameter is null";

		String message = "There are more optional parameters. \n Do you want to fill them?";

		ResponseMessage res = new ResponseMessage(message);
		return (res);
	}

	// Slot Intents

	@Override
	protected ResponseMessage getRequest(DialogueAct act) {

		assert act != null : "dialogue act parameter is null";
		assert act.getEntities() != null : "dialogue act has no entities";
		assert act.getEntities().containsKey("name") : "dialogue act has no name entitiy";

		String message = "";
		Map<String, String> entities = act.getEntities();
		System.out.println(entities.entrySet());

		String name = entities.get("name");
		message = message + "What is the *" + name + "*? \n\n";
		System.out.println(message);

		if (entities.containsKey("description"))
			message = message + entities.get("description") + "\n";

		if (entities.containsKey("example"))
			message = message + "Example:    \t" + entities.get("example") + "\n";

		if (act.hasExpected() && act.getExpected().getType() != null)
			message = message.concat("\n" + this.InputTypeMessage(act.getExpected()) + "\n");
		if (act.getExpected().hasEnums()) {
			List<String> enums = act.getExpected().getEnums();
			message = message.concat(enums.get(0));
			for (String enu : enums.subList(1, enums.size()))
				message = message + ", " + enu;
		}

		ResponseMessage res = new ResponseMessage(message);
		return res;
	}

	@Override
	protected ResponseMessage getInform(DialogueAct act) {

		assert act != null : "dialogue act parameter is null";
		assert act.getEntities() != null : "dialogue act has no entities";
		assert act.getEntities().containsKey("name") : "dialogue act has no name entitiy";

		String message = "";
		Map<String, String> entities = act.getEntities();

		String name = entities.get("name");
		message = "*" + name + "*";

		if (entities.containsKey("description"))
			message = message.concat("description:\t" + entities.get("description") + "\n");

		if (entities.containsKey("value"))
			message = message.concat("current value:\t" + entities.get("value") + "\n");

		ResponseMessage res = new ResponseMessage(message);
		return res;
	}

	protected String InputTypeMessage(ExpectedInput inputType) {
		assert inputType != null : "inputType parameter is null";
		assert inputType.getType() != null : "inputType has no type";

		String message = "";
		switch (inputType.getType()) {

		case Confirmation:
			message = "Please confirm or deny";
			break;
		case Date:
			message = "Please give a date in the format \"yyyy-MM-dd\" ";
			break;
		case Decimal:
		case Number:
			message = "Please give a number";
			break;
		case Enum:
			message = "Please choose one of this possibilites: ";
			break;
		case Free:
			message = "Please answer with a free text message";
			break;
		case Url:
			message = "Please answer with a valid url";
			break;
		case Word:
			message = "Please answer with one word";
			break;
		default:
			break;

		}

		return message;
	}

	@Override
	protected ResponseMessage getMainMenu(DialogueAct act) {

		assert act != null : "dialogue act parameter is null";
		assert act.getEntities() != null : "dialogue act has no entities";
		
		String message = "";
		Map<String, String> entities = act.getEntities();
		String botName = entities.get("botName");
		String botDesc = entities.get("botDescription");
		if(botName != null)
			message = message + "Hi, I am " + botName + " 🤖 \n";
		else
			message = message + "Hi, I am a bot 🤖 \n";
		
		if(botDesc != null)
			message = message + botDesc + "\n";
		
		message = message + "\nI can perform the following operations: \n";
		
		for (Entry<String, String> entity : entities.entrySet()) {
			assert entity.getKey() != null : "entity no key";
			assert entity.getValue() != null : "entity no value";
			if(!entity.getKey().contentEquals("botName") && !entity.getKey().contentEquals("botDescription"))
				message = message.concat("/" + entity.getKey() + " - " + entity.getValue()).concat("\n");
		}

		message = message.concat(
				"\n During conversation you can use the following commands: \n /cancel Aborts the current operation \n /revert Reverts your last input.");

		ResponseMessage res = new ResponseMessage(message);
		return res;
	}
	
}
