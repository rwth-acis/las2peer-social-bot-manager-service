package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Fillable;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Node;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.RepetitionNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Slotable;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;
import i5.las2peer.services.socialBotManagerService.parser.openapi.RepeatingNode;

public class TaskOrientedManagerImpl extends TaskOrientedManager {

	DialogueGoal goal;
	DialogueActGenerator gen;
	boolean optional;

	public TaskOrientedManagerImpl(DialogueGoal goal) {
		this.goal = goal;
		this.gen = new DialogueActGenerator();
	}

	@Override
	public DialogueAct handle(Intent semantic) {

		assert semantic != null : "naive dm handle: semantic is null";
		assert semantic.getKeyword() != null : "naive dm handle: semantic has no intent";
		assert semantic.getIntentType() != null : "no intent type set";

		// first call
		String intent = semantic.getKeyword();
		if (intent.equalsIgnoreCase(getStartIntent())) {
			handleEntities(semantic);
			System.out.println("first call " + this.getFrame().getIntent() + " ready: " + goal.isReady()
					+ " fillables: " + this.goal.getAll().Fillables().size());
			if (!goal.isReady())
				return requestNextSlot();
			return perform();
		}

		// get corresponding slot
		Slotable slo = null;
		if (goal.contains(intent)) {
			slo = goal.getNode(intent);
			System.out.println("corresponding slot found: " + slo.getAPIName() + " " + slo.getClass());
		} else
			System.out.println("naive dm handle: slot not found for intent: " + intent);

		// Repetition Node
		if (slo instanceof RepetitionNode) {
			RepetitionNode rep = (RepetitionNode) slo;
			if (semantic.getIntentType() == IntentType.CONFIRM) {
				rep.extend();
				return requestNextSlot();
			}
			if (semantic.getIntentType() == IntentType.DENY) {
				rep.close();
				if (goal.isFull())
					return gen.getReqConfAct(goal);
				
				// ask if optional slots should be filled
				if (!goal.isFull() && goal.isReady()) {
					if(goal.getFrame().getName().contentEquals("createbot")) {
						return perform();
					}
					
					DialogueAct act = gen.getReqOptionalAct(goal);
					this.optional = true;
					return act;
				}
				System.out.println("request next slot because deny proceed");				
				return requestNextSlot();
			}
		}

		// Value Nodes
		Fillable node = (Fillable) slo;
		DialogueAct act = new DialogueAct();
		switch (semantic.getIntentType()) {
		case INFORM:

			// fill slot
			boolean filled = false;
			for (Entity entity : semantic.getEntities()) {
				String value = entity.getValue();
				if (node.validate(value)) {
					System.out.println("node " + node.getName() + " filled with " + value);
					if (this.goal.fill(node, value))
						filled = true;
				}
			}

			// Nothing was filled, request again
			if (!filled) {				
				return requestNextSlot();
			}

			// arrays
			if (node != null && node.getSlot().isArray()) {
				
				if (node instanceof RepeatingNode) {
					RepeatingNode rep = (RepeatingNode) node;
					System.out.println("repeating node " + node.getAPIName() + " size " + rep.size() + " min "
							+ rep.getMinItems());
					if (rep.getMinItems() > rep.size())
						return requestNextSlot();					
				}
				
				return gen.getReqConfArrayAct(node);

			} else if (!optional && goal.isReady()) {
				System.out.println("size fillable: " + goal.getAll().Fillables().size());
				if (goal.getAll().Fillables().size() < 2)
					return perform();
				System.out.println("size fillable: " + goal.getAll().Fillables().size());
				return gen.getReqConfAct(goal);
			}

			// check if full
			if (goal.isFull())
				return gen.getReqConfAct(goal);

			return requestNextSlot();

		case REQUEST:

			// inform about filled slot value
			if (node != null)
				return gen.getInformAct(node);

			// inform about expected slot value
			// TODO

		case CONFIRM:

			// user confirms that collected slot information is correct
			if (semantic.getKeyword().contentEquals(goal.getFrame().getConfirmIntent())) {

				// ask if optional slots should be filled
				if (!goal.isFull() && goal.isReady()) {
					act = gen.getReqOptionalAct(goal);
					this.optional = true;
					return act;
				}

				// perform the action
				System.out.println("perform by confirm");
				return perform();
			}

			// user want to fill further optional slots
			if (semantic.getKeyword().contentEquals(goal.getFrame().getConfirmIntent().concat("_optional")))
				return requestNextSlot();

			// user wants to fill more values for same slot
			if (node != null && semantic.getKeyword().contentEquals(node.getReqConfProceed()))
				return gen.getRequestAct(node, goal);

			// user confirm but bot dont know why
			return requestNextSlot();

		case DENY:

			// deny that collected information is correct
			if (semantic.getKeyword().contentEquals(goal.getFrame().getConfirmIntent())) {

				// delete collected information and start all over
				this.reset();
				return requestNextSlot();
			}

			// deny optional frame
			if (semantic.getKeyword().contentEquals(goal.getFrame().getConfirmIntent().concat("_optional")))
				return perform();

			// deny array slot
			if (node != null && semantic.getKeyword().contentEquals(node.getReqConfProceed())) {

				// check if ready
				if (!optional && goal.isReady())
					return gen.getReqConfAct(goal);

				// request next slot
				return requestNextSlot();
			}

			// User wants so delete specific slot
			if (node != null && node.isFilled()) {
				goal.delete(node);
				return gen.getInformAct(node);
			}

		default:
			// request next slot
			return new DialogueAct("naive dm default with known intent");
		}
	}

	@Override
	public boolean hasIntent(String intent) {
		assert intent != null : "intent parameter is null";
		assert getStartIntent() != null : "no start intent defined";

		if (intent.equalsIgnoreCase(getStartIntent()))
			return true;
		if (intent.contentEquals(goal.getFrame().getConfirmIntent())
				|| intent.contentEquals(goal.getFrame().getConfirmIntent() + "_optional"))
			return true;
		return goal.contains(intent);
	}

	@Override
	public void reset() {
		goal.reset();
		optional = false;
	}

	private DialogueAct requestNextSlot() {
		assert goal != null : "goal is null";
		assert !goal.isFull() : "goal is already full";
		
		Node nextNode = goal.next();
		System.out.println("request next node " + nextNode.getClass());
		// Request to fill value
		if (nextNode instanceof Fillable) {
			Fillable fi = (Fillable) nextNode;
			return gen.getRequestAct(fi, goal);
		}

		// next repetition of node
		if (nextNode instanceof RepetitionNode) {
			RepetitionNode rep = (RepetitionNode) nextNode;

			// min values not reached yet
			//System.out.println("min values = " + rep.getSlot().getParameter().getMinItems() + " , size "
			//		+ rep.getValueChildren().size());
			//if (rep.getSlot().getParameter().getMinItems() > rep.getValueChildren().size()) {
			//	rep.extend();
			//	return requestNextSlot();
			//}

			// ask if should repeat
			return gen.getReqConfArrayAct(rep);
		}

		assert false : "next slot is no Fillable nor Repetition node: " + nextNode.getClass();
		return null;
	}

	private DialogueAct perform() {
		DialogueAct act = new DialogueAct();
		act.setGoal(goal);
		act.setAction(goal.getOpenAPIAction());
		if (goal.getFrame().getFile() != null)
			act.setFile(goal.getFrame().getFile());
		this.reset();
		return act;

	}

	private void handleEntities(Intent semantic) {
		assert semantic != null;

		if (semantic.hasEntity()) {
			for (Entity entity : semantic.getEntities()) {
				Fillable node = goal.getNodeByEntity(entity);
				if (node != null && entity.getValue() != null && !entity.getValue().contentEquals("")) {
					goal.fill(node, entity.getValue());
					System.out.println("entity " + entity.getEntityName() + " of node " + node.getName()
							+ " filled with " + entity.getValue());
				}
			}
		}

	}

	@Override
	public Collection<String> getNLUIntents() {
		Collection<String> intents = new ArrayList<String>();

		intents.add(getStartIntent());
		intents.add(goal.getFrame().getConfirmIntent());
		intents.add(goal.getFrame().getConfirmIntent() + "_optional");

		Collection<Slot> slots = goal.getFrame().getSlots().values();
		for (Slot slot : slots) {
			intents.add(slot.getInformIntent());
		}

		return intents;
	}

	@Override
	public Collection<String> getNLGIntents() {
		Collection<String> intents = new ArrayList<String>();

		Frame frame = goal.getFrame();
		intents.add(frame.getReqConfIntent());
		if (frame.hasOptionalSlots())
			intents.add(frame.getReqConfIntent() + "_optional");

		for (Slot slot : frame.getDescendants()) {
			if (slot.isLeaf())
				intents.add(slot.getRequestIntent());
			if (slot.isArray())
				intents.add(slot.getReqConfIntent());
		}

		return intents;
	}

	@Override
	public DialogueGoal getDialogueGoal() {
		return goal;
	}

	@Override
	public void fillRecursive(String attrId, String value) {
		assert attrId != null;
		assert value != null;
		assert !attrId.contentEquals("");

		if (this.goal.fill(attrId, value))
			System.out.println("slot attribute " + attrId + " of frame " + this.goal.getFrame().getIntent()
					+ " was external filled with " + value);
	}

	@Override
	public String getStartIntent() {
		return this.goal.getFrame().getIntentKeyword();
	}

}