package i5.las2peer.services.socialBotManagerService.dialogue.nlg;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;

public class GermanMessageGenerator extends DefaultMessageGenerator {

	@Override
	protected ResponseMessage getErrorSystem(DialogueAct act) {
		return new ResponseMessage(
				"Tut mir Leid. Mein System hat einen Fehler 🙈. Wir müssen das Gespräch neu starten");
	}

	@Override
	protected ResponseMessage getErrorNLU(DialogueAct act) {
		return new ResponseMessage(
				"Ich kann zurzeit leider keine Sprache verstehen 🙉. Bitte benutz explizite Befehle 😉");
	}

	@Override
	protected ResponseMessage getErrorCommandUnknown(DialogueAct act) {
		return new ResponseMessage("Den Befehl kenne ich leider nicht 🤔");
	}

	@Override
	protected ResponseMessage getErrorInvalidInput(DialogueAct act) {
		assert act != null;

		String message = "Danach habe ich nicht gefragt";
		switch (act.getExpected().getType()) {
		case Binary:
			break;
		case Confirmation:
			message = "Bitte gib mir eine klare Antwort ob du zustimmst 👍 oder ablehnst 👎";
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
			message = "Bitte wähle eine von diesen Möglichkeiten aus: \n";
			for (String enu : act.getExpected().getEnums()) {
				message = message + enu + "\n";
			}
			break;
		case Free:
			break;
		case Number:
			message = "Bitte antworte mit einer Nummer 1️⃣2️⃣3️⃣ \n";
			break;
		case Url:
			message = "Bitte antworte mit einer Internetaddresse \n Sie sollte mit 'http' anfangen.";
			break;
		case Word:
			message = "Bitte antworte in einem Wort ohne Leerzeichen \n";
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
		String message = "Möchtest du weitere hinzufügen? (*" + name + "*)";
		if(isMaskulin(name))
			message = "Möchtest du einen weiteren *" + name + "* hinzufügen?";
		else if(isFeminine(name))
			message = "Möchtest du eine weitere *" + name + "* hinzufügen?";
		else if(isNeuter(name))
			message = "Möchtest du ein weiteres *" + name + "* hinzufügen?";
		ResponseMessage res = new ResponseMessage(message);
		return res;
	}

	// Frame Intents

	@Override
	protected ResponseMessage getReqConf(DialogueAct act) {
		assert act != null;

		String message = "Ich habe nun alle Informationen die ich brauche 😄 \n\n";
		for (Map.Entry<String, String> entry : act.getEntities().entrySet())
			message = message.concat(entry.getKey()).replaceAll("_", " ").concat(": \t ").concat(entry.getValue())
					.concat(" \n");
		message = message.concat("\n Wenn das richtig ist, soll ich die Aktion ausführen? \n");

		ResponseMessage res = new ResponseMessage(message);
		return (res);
	}

	@Override
	protected ResponseMessage getReqOptional(DialogueAct act) {
		return new ResponseMessage("Es gibt weitere optionale Parameter \n Möchtest du diese auch angeben?");
	}

	// Slot Intents

	@Override
	protected ResponseMessage getRequest(DialogueAct act) {

		assert act != null : "dialogue act parameter is null";
		assert act.getEntities() != null : "dialogue act has no entities";
		assert act.getEntities().containsKey("name") : "dialogue act has no name entitiy";

		String message = "";
		Map<String, String> entities = act.getEntities();

		String name = entities.get("name");
		if(isMaskulin(name))
			message = message + "Wie lautet der *" + name + "*? \n\n";
		else if(isFeminine(name))
			message = message + "Wie lautet die *" + name + "*? \n\n";
		else if(isNeuter(name))
			message = message + "Wie lautet das *" + name + "*? \n\n";
		else
			message = message + "Ich brauche folgende Information: *" + name + "*? \n\n";
		

		if (entities.containsKey("description"))
			message = message + entities.get("description") + "\n";

		if (entities.containsKey("example"))
			message = message + "Beispiel:    \t" + entities.get("example") + "\n";

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
			message = message.concat(entities.get("description") + "\n");

		if (entities.containsKey("value"))
			message = message.concat("aktueller Wert:\t" + entities.get("value") + "\n");

		ResponseMessage res = new ResponseMessage(message);
		return res;
	}

	protected String InputTypeMessage(ExpectedInput inputType) {
		assert inputType != null : "inputType parameter is null";
		assert inputType.getType() != null : "inputType has no type";

		String message = "";
		switch (inputType.getType()) {

		case Confirmation:
			message = "Bitte bestätige oder verneine";
			break;
		case Date:
			message = "Bitte gebe ein Datum im folgenden Format an \"yyyy-MM-dd\" ";
			break;
		case Decimal:
		case Number:
			message = "Bitte gebe eine Nummer an";
			break;
		case Enum:
			message = "Bitte wähle eine dieser Möglichkeiten: ";
			break;
		case Free:
			message = "Du kannst frei antworten";
			break;
		case Url:
			message = "Bitte antworte mit einer Internetadresse";
			break;
		case Word:
			message = "Bitte antworte mit einem zusammenhängenden Wort";
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
		if (botName != null)
			message = message + "Hallo, Ich bin " + botName + " 🤖 \n";
		else
			message = message + "Hallo, Ich bin ein Bot 🤖 \n";

		if (botDesc != null)
			message = message + botDesc + "\n";

		message = message + "\n Ich kann die folgenden Aufgaben übernehmen: \n";

		for (Entry<String, String> entity : entities.entrySet()) {
			assert entity.getKey() != null : "entity no key";
			assert entity.getValue() != null : "entity no value";
			if (!entity.getKey().contentEquals("botName") && !entity.getKey().contentEquals("botDescription"))
				message = message.concat("/" + entity.getKey() + " - " + entity.getValue()).concat("\n");
		}

		message = message.concat(
				"\n Außerdem kannst du im Gespräch mit mir folgende Befehle benutzen: \n /cancel Bricht die aktuelle Aktion ab \n /revert Macht deine letzte Eingabe rückgängig");

		ResponseMessage res = new ResponseMessage(message);
		return res;
	}

	private boolean isMaskulin(String noun) {
		String[] endings = { "ling", "ismus", "ich", "ig", "eig", "ent", "ast", "or", "and", "ant", "är", "end", "eur",
				"ör", "ier", "iker", "ist", "oge", "ice", "ype", "tus", "name" };
		for (String ending : endings) {
			if (noun.endsWith(ending))
				return true;			
		}
		return false;
	}

	private boolean isFeminine(String noun) {
		String[] endings = { "eit", "ung", "schaft", "in", "ion", "erei", "anz", "enz", "ik", "ität", "itis", "ur", "id" };
		for (String ending : endings) {
			if (noun.endsWith(ending))
				return true;			
		}
		return false;
	}

	private boolean isNeuter(String noun) {
		String[] endings = { "chen", "lein", "tum", "um", "ett", "ma", "ment", "et" };
		for (String ending : endings) {
			if (noun.endsWith(ending))
				return true;
		}
		return false;
	}

}
