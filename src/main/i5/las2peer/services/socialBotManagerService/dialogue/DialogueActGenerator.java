package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Fillable;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.MultiValueNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Repeatable;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Slotable;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.Domain;
import i5.las2peer.services.socialBotManagerService.model.DynamicResponse;
import i5.las2peer.services.socialBotManagerService.model.GeneratorFunction;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIResponse;

public class DialogueActGenerator {

	public static DialogueAct getAct(String intent) {
		DialogueAct res = new DialogueAct();
		res.setIntent(intent);
		return res;
	}

	/**
	 * get a generic response act.
	 * 
	 * @param element
	 * @return
	 */
	public static DialogueAct getAct(DynamicResponse element) {

		return getAct(element.getActIntent());

	}

	//// Frame Dialogue Acts

	public DialogueAct getReqConfAct(DialogueGoal goal) {

		assert goal != null : "node parameter is null";
		assert goal.getFrame() != null : "root node has no frame";
		assert goal.isReady() : "goal is not ready yet";

		// intent and entities
		DialogueAct act = new DialogueAct();
		act.setIntent(goal.getFrame().getReqConfIntent());
		act.setIntentType(DialogueActType.REQCONF_FRAME);
		List<Fillable> values = goal.getAll().getFilledNode();
		for (Fillable node : values)
			act.addEntity(node.getSlot().getDisplayName(), node.getValue());

		// expected input
		ExpectedInput input = new ExpectedInput();
		input.setIntend(goal.getFrame().getConfirmIntent());
		input.setType(InputType.Confirmation);
		input.addEnum("yes");
		input.addEnum("no");
		act.setExpected(input);
		return (act);
	}

	public DialogueAct getReqOptionalAct(DialogueGoal goal) {

		assert goal != null : "node parameter is null";
		assert goal.getFrame() != null : "root node has no frame";
		assert goal.isReady() : "goal is not ready yet";

		// intent and entities
		DialogueAct act = new DialogueAct();
		act.setIntent(goal.getFrame().getReqConfIntent() + "_optional");
		act.setIntentType(DialogueActType.REQCONF_FRAME_OPTIONAL);

		// expected input
		ExpectedInput input = new ExpectedInput();
		input.setIntend(goal.getFrame().getConfirmIntent() + "_optional");
		input.setType(InputType.Confirmation);
		input.addEnum("yes");
		input.addEnum("no");
		act.setExpected(input);
		return (act);
	}

	//// Slot Dialogue Acts
	public DialogueAct getRequestAct(Fillable node, DialogueGoal goal) {

		assert node != null : "slot parameter is null";
		assert node.getSlot() != null : "node has no slot";

		// intent
		Slot slot = node.getSlot();
		DialogueAct act = new DialogueAct();
		act.setIntent(slot.getRequestIntent());
		act.setIntentType(DialogueActType.REQUEST_SLOT);
		act.addEntity("name", slot.getDisplayName());

		// variables
		if (slot.getParameter().getDescription() != null)
			act.addEntity("description", slot.getParameter().getDescription());
		if (slot.getParameter().getExample() != null)
			act.addEntity("example", slot.getParameter().getExample());

		if (goal != null) {
			System.out.println("#variables for already filled values");
			for (Entry<String, String> entry : goal.getFilledValues().entrySet()) {
				act.addEntity("value_" + entry.getKey(), entry.getValue());
			}

		}

		// repeatable
		if (node instanceof Repeatable) {

			Repeatable rep = (Repeatable) node;
			act.addEntity("needed", String.valueOf(rep.getNeeded()));
			act.addEntity("size", String.valueOf(rep.size()));
			if (rep.size() >= 1) {
				act.setIntent(slot.getRequestIntent() + "_next");
				if (rep.size() >= 1 && rep.getNeeded() < 1)
					act.setIntent(slot.getRequestIntent() + "_additional");

				if (node instanceof MultiValueNode) {
					System.out.println("#instance of multi value node");
					MultiValueNode mv = (MultiValueNode) node;
					String values = "";
					for (String value : mv.getValues()) {
						values = values + value + ", ";
					}
					act.addEntity("values", values);
				}
			}
		}

		// expected input
		ExpectedInput input = new ExpectedInput();
		input.setType(slot.getInputType());
		input.setIntend(slot.getInformIntent());
		input.setEntity(slot.getEntity());

		if (node.getSlot().getParameter() != null && node.getSlot().getParameter().getInput() != null)
			input.setParameterInput(node.getSlot().getParameter().getInput());

		if (slot.hasDynamicFormat()) {

			Map<String, String> parameters = goal.getFunctionParametersOfNode(node);
			String format = slot.getUpdatedFormat(parameters);
			System.out.println(node.getName() + " has dynamic format " + format);
			input.setType(InputType.fromString(format));

		} else {

			if (slot.getInputType() == InputType.Enum) {

				List<String> enumList = new ArrayList<>();
				if (slot.hasDynamicEnums()) {

					Map<String, String> parameters = goal.getFunctionParametersOfNode(node);
					enumList = node.getSlot().getParameter().getUpdatedEnumList(parameters);

				} else {
					// slot.update();
					enumList = slot.getEnumList();
				}

				if (enumList != null)
					for (String enu : enumList)
						input.addEnum(enu);
			}
		}

		if (slot.getParameter().getRetrieveFunction() != null) {
			Map<String, String> parameters = goal.getFunctionParametersOfNode(node);
			OpenAPIAction action = new OpenAPIAction(slot.getParameter().getRetrieveFunction(), parameters);
			OpenAPIResponse response = action.execute();
			if (response.isSuccess()) {
				Map<String, String> entities = GeneratorFunction.getEntities(response.getAsJSON());
				for (Entry<String, String> en : entities.entrySet())
					act.addEntity(en.getKey(), en.getValue());
			}

			if (input.getEnums() == null || input.getEnums().isEmpty())
				if (slot.getParameter().getContentFill() != null
						&& !slot.getParameter().getContentFill().contentEquals("")) {
					if (slot.getParameter().getContentURLKey() != null) {
						Collection<String> enumList = OpenAPIConnector.searchValuesByKey(response.getAsJSON(),
								slot.getParameter().getContentURLKey());
						if (enumList != null)
							for (String enu : enumList)
								input.addEnum(enu);
					}
				}

		}

		if (slot.getInputType() == InputType.File && slot.getParameter().getFile() != null)
			act.addEntity("fileType", slot.getParameter().getFile().getType());

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
		input.addEnum("yes");
		input.addEnum("no");
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

		// repeatable
		if (node instanceof Repeatable) {

			Repeatable rep = (Repeatable) node;
			act.addEntity("needed", String.valueOf(rep.getNeeded()));
			act.addEntity("size", String.valueOf(rep.size()));

			if (node instanceof MultiValueNode) {
				System.out.println("#instance of multi value node");
				MultiValueNode mv = (MultiValueNode) node;
				String values = "";
				for (String value : mv.getValues()) {
					values = values + value + ", ";
				}
				act.addEntity("values", values);

			}
		}

		ExpectedInput input = new ExpectedInput();
		input.setIntend(node.getReqConfProceed());
		input.setType(InputType.Confirmation);
		input.addEnum("yes");
		input.addEnum("no");
		act.setExpected(input);
		return act;
	}

	// System acts

	public DialogueAct getMainMenuAct(Messenger messenger) {

		Bot bot = messenger.getBot();
		DialogueAct act = new DialogueAct();
		act.setIntent("start");
		act.setIntentType(DialogueActType.SYSTEM_HOME);

		for (Domain domain : messenger.getDomains().values()) {
			act.addEntity(domain.getName(), "domainName", domain.getName());
			act.addEntity(domain.getName(), "domainDescription", domain.getDescription());
			for (Command operation : domain.getCommands())
				act.addEntity(domain.getName(), operation.getName(), operation.getDescription());
		}

		if (bot != null) {
			act.addEntity("botName", bot.getName());
			act.addEntity("botDescription", bot.getDescription());
		}

		return act;
	}

	// Invalid input acts

	public static DialogueAct getInvalidValueAct(ExpectedInput input) {

		DialogueAct act = new DialogueAct();
		act.setIntent(input.getIntend() + "_invalid");
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
