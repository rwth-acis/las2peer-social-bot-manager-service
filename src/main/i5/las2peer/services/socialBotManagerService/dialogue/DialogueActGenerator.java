package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.List;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Fillable;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.RootNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Slotable;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;

public class DialogueActGenerator {

	public DialogueAct getAct() {
		return new DialogueAct();
	}

	//// Frame Dialogue Acts

	public DialogueAct getReqConfAct(RootNode root) {

		assert root != null : "node parameter is null";
		assert root.getFrame() != null : "root node has no frame";
		assert root.isReady() : "goal is not ready yet";

		// intent and entities
		DialogueAct act = new DialogueAct();
		act.setIntent(root.getFrame().getReqConfIntent());
		act.setIntentType(DialogueActType.REQCONF_FRAME);
		List<Fillable> values = root.getAll().getFilledValues();
		for (Fillable node : values)
			act.addEntity(node.getSlot().getDisplayName(), node.getValue());

		// expected input
		ExpectedInput input = new ExpectedInput();
		input.setIntend(root.getFrame().getConfirmIntent());
		input.setType(InputType.Confirmation);
		act.setExpected(input);
		return (act);
	}

	public DialogueAct getReqOptionalAct(RootNode root) {

		assert root != null : "node parameter is null";
		assert root.getFrame() != null : "root node has no frame";
		assert root.isReady() : "goal is not ready yet";

		// intent and entities
		DialogueAct act = new DialogueAct();
		act.setIntent(root.getFrame().getReqConfIntent() + "_optional");
		act.setIntentType(DialogueActType.REQCONF_FRAME_OPTIONAL);

		// expected input
		ExpectedInput input = new ExpectedInput();
		input.setIntend(root.getFrame().getConfirmIntent() + "_optional");
		input.setType(InputType.Confirmation);
		act.setExpected(input);
		return (act);
	}

	//// Slot Dialogue Acts

	public DialogueAct getRequestAct(Fillable node) {

		assert node != null : "slot parameter is null";
		assert node.getSlot() != null : "node has no slot";

		// intent
		Slot slot = node.getSlot();
		DialogueAct act = new DialogueAct();
		act.setIntent(slot.getRequestIntent());
		act.setIntentType(DialogueActType.REQUEST_SLOT);
		act.addEntity("name", slot.getDisplayName());
		if (slot.getParameter().getDescription() != null)
			act.addEntity("description", slot.getParameter().getDescription());
		if (slot.getParameter().getExample() != null)
			act.addEntity("example", slot.getParameter().getExample());

		// expected input
		ExpectedInput input = new ExpectedInput();
		input.setType(slot.getInputType());
		input.setIntend(slot.getInformIntent());
		input.setEntity(slot.getEntity());
		if (slot.getInputType() == InputType.Enum) {
			slot.update();
			List<String> enumList = slot.getEnumList();
			for (String enu : enumList) {
				input.addEnum(enu);
			}
		}
		act.setExpected(input);
		return act;

	}

	public DialogueAct getInformAct(Fillable node) {

		assert node != null : "node parameter is null";
		assert node.getSlot() != null : "node has no slot";

		Slot slot = node.getSlot();
		DialogueAct act = new DialogueAct();
		act.setIntent(slot.getInformIntent());
		act.setIntentType(DialogueActType.INFORM_SLOT);
		act.addEntity(slot.getEntity(), node.getValue());

		return act;
	}

	public DialogueAct getConfirmAct(Fillable node) {

		assert node != null : "node parameter is null";
		assert node.getSlot() != null : "node has no slot";

		Slot slot = node.getSlot();
		DialogueAct act = new DialogueAct();
		act.setIntent(slot.getConfirmIntent());
		act.setIntentType(DialogueActType.REQCONF_SLOT);
		act.addEntity(slot.getName(), node.getValue());

		ExpectedInput input = new ExpectedInput();
		input.setIntend(slot.getConfirmIntent());
		input.setType(InputType.Confirmation);
		return (act);
	}

	public DialogueAct getReqConfArrayAct(Slotable node) {

		assert node != null : "node parameter is null";

		String name = node.getDisplayName();
		DialogueAct act = new DialogueAct();
		if (name.charAt(name.length() - 1) == 's') {
			name = name.substring(0, name.length() - 1);
		}
		act.addEntity(new Entity("name", name));
		act.setIntentType(DialogueActType.REQCONF_SLOT_PROCEED);
		act.setIntent(node.getReqConfProceed());

		ExpectedInput input = new ExpectedInput();
		input.setIntend(node.getReqConfProceed());
		input.setType(InputType.Confirmation);
		act.setExpected(input);
		return act;
	}

	// System acts
	
	public DialogueAct getMainMenuAct(Bot bot, List<Command> operations) {

		assert operations != null : "commands is null";
		assert !operations.isEmpty() : "commands are empty";

		DialogueAct act = new DialogueAct();
		act.setIntent("start");
		act.setIntentType(DialogueActType.SYSTEM_HOME);
		for (Command operation : operations) {
			operation.invariant();
			act.addEntity(operation.getName(), operation.getDescription());
		}
		
		if(bot != null) {
			act.addEntity("botName", bot.getName());
			act.addEntity("botDescription", bot.getDescription());
		}
		
		return act;
	}
	
	// Invalid input acts
	
	public static DialogueAct getInvalidValueAct(ExpectedInput input) {

		DialogueAct act = new DialogueAct();
		act.setExpected(input);
		act.setIntentType(DialogueActType.ERROR_INVALID_INPUT);
		return act;

	}

	public DialogueAct getUnknownCommandAct() {

		DialogueAct act = new DialogueAct();
		act.setIntentType(DialogueActType.ERROR_COMMAND_UNKNOWN);
		return act;
	}
	
	// Internal error acts
	
	public static DialogueAct getNLUErrorAct() {
		
		DialogueAct act = new DialogueAct();
		act.setIntentType(DialogueActType.ERROR_NLU);		
		return act;		
	}
	
	
	public static DialogueAct getSystemErrorAct() {
		
		DialogueAct act = new DialogueAct();
		act.setIntentType(DialogueActType.ERROR_SYSTEM);	
		return act;		
	}
	
}
