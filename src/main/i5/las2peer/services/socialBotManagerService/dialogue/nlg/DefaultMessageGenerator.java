package i5.las2peer.services.socialBotManagerService.dialogue.nlg;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;

public abstract class DefaultMessageGenerator extends LanguageGenerator {
	
	@Override
	public ResponseMessage parse(DialogueAct act) {

		assert act != null : "dialogue act parameter is null";
		assert act.getIntent() != null : "dialogue act has no intent";
		assert act.getIntentType() != null : "no intent type specified";

		System.out.println("get default response for intent " + act.getIntent() + " of type " + act.getIntentType());

		switch (act.getIntentType()) {
		case SYSTEM_HOME:
			return getMainMenu(act);		
		case REQCONF_FRAME:
			return getReqConf(act);
		case REQCONF_FRAME_OPTIONAL:
			return getReqOptional(act);
		case REQUEST_SLOT:
			return getRequest(act);	
		case REQCONF_SLOT:
			break;
		case REQCONF_SLOT_PROCEED:
			return getReqConfSlotProceed(act);
		case INFORM_SLOT:
			return getInform(act);
		case ERROR_COMMAND_UNKNOWN:
			return getErrorCommandUnknown(act);
		case ERROR_INVALID_INPUT:
			return getErrorInvalidInput(act);
		case ERROR_NLU:
			return getErrorNLU(act);
		case ERROR_SYSTEM:
			return getErrorSystem(act);		
		case TALK:
			break;
		default:
			break;

		}
		return null;

	}

	protected abstract ResponseMessage getErrorSystem(DialogueAct act);

	protected abstract ResponseMessage getErrorNLU(DialogueAct act);

	protected abstract ResponseMessage getErrorInvalidInput(DialogueAct act);

	protected abstract ResponseMessage getErrorCommandUnknown(DialogueAct act);

	protected abstract ResponseMessage getInform(DialogueAct act);

	protected abstract ResponseMessage getReqConfSlotProceed(DialogueAct act);

	protected abstract ResponseMessage getRequest(DialogueAct act);

	protected abstract ResponseMessage getReqOptional(DialogueAct act);

	protected abstract ResponseMessage getReqConf(DialogueAct act);

	protected abstract ResponseMessage getMainMenu(DialogueAct act);
		
	protected String escape(String text) {

		String res = text.replaceAll("\\*", "\\\\*").replace("\\", "\\\\").replace("_", "\\_");
		res = res.replaceAll("#", "\\#").replaceAll("\\+", "\\\\+").replaceAll("-", "\\-");
		return res;
	}
}
