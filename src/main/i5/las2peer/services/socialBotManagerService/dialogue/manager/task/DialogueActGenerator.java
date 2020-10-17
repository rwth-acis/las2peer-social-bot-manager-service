package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import java.util.List;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActType;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Fillable;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.RootNode;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class DialogueActGenerator {

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
	    act.addEntity(node.getSlot().getName(), node.getValue());

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
	act.setIntentType(DialogueActType.REQCONF_OPTIONAL);

	// expected input
	ExpectedInput input = new ExpectedInput();
	input.setIntend(root.getFrame().getConfirmIntent() + "_optional");
	input.setType(InputType.Confirmation);
	act.setExpected(input);
	return (act);
    }

    public DialogueAct getRequestAct(Fillable node) {

	assert node != null : "slot parameter is null";
	assert node.getSlot() != null : "node has no slot";

	// intent
	Slot slot = node.getSlot();
	DialogueAct act = new DialogueAct();
	act.setIntent(slot.getRequestIntent());
	act.setIntentType(DialogueActType.REQUEST_SLOT);
	act.addEntity("name", slot.getAPIName());
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
	    for (String enu : slot.getEnumList()) {
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

}
