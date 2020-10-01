package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import java.util.Collection;
import java.util.Iterator;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInputType;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.AbstractDialogueManager;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class AgendaDialogueManager extends AbstractDialogueManager {

	AgendaDialogueStack stack;
	AgendaDialogueNode root;
	DialogueState state;
	Frame goal;
	public String goalMessage;

	public AgendaDialogueManager() {

	}

	@Override
	public synchronized DialogueAct handle(Intent semantic) {
		System.out.println("ADM: handle intent: " + semantic.getKeyword() + " entity: " + semantic.getEntities());

		String intent = semantic.getKeyword();
		Iterator<AgendaDialogueNode> iterator = stack.iterator();
		while (iterator.hasNext()) {
			AgendaDialogueNode node = iterator.next();
			System.out.println("------ handle " + node.getIntent());
			if (node.hasIntent(intent)) {

				System.out.println(semantic);
				// Identify entities as slot values
				if (semantic.getEntities() != null) {
					for (String entity : semantic.getEntitieValues()) {

						this.state.addSlotValue(node.getIntent(), entity);
						System.out.println("Slot received: " + entity);
						System.out.println(this.state.slotValues);

					}
					stack.remove(node);
				}

				DialogueAct act = handle(node);
				String response = act.getMessage();

				// Check if all information is collected
				if (isFilled()) {
					response = this.goalMessage.concat(" : ");
					System.out.println("values: " + this.state.slotValues.size());
					for (String value : this.state.slotValues.values()) {
						System.out.println(value);
						response = response.concat(", ").concat(value);
					}
					this.reset();
					act.setFull(true);
					act.setMessage(response);
					return act;
				}

				act.setMessage(response);
				return act;
			}
		}

		return this.handleDefault();
	}

	public DialogueAct handle(AgendaDialogueNode node) {
		String response = "";
		System.out.println("------ -------- handle " + node.getIntent());
		// node with children
		if (node.hasChildren()) {
			stack.remove(node);
			// Add node response
			if (!node.getResponses().isEmpty()) {
				response = response.concat(node.getResponses().get(0));
				response = response.concat("\n");
			}
			stack.addAll(0, node.getChildren());
			AgendaDialogueNode subNode = node.getChildren().get(0);
			DialogueAct act = new DialogueAct(response).concat(handle(subNode));
			return act;

			// leaf node
		} else {

			AgendaDialogueNode next = next();
			String intent = next.getIntent();
			ExpectedInputType type = next.getInputType();
			response = response.concat(next.getResponses().get(0));

			DialogueAct act = new DialogueAct();
			act.setMessage(response);
			ExpectedInput input = new ExpectedInput(type, intent);

			act.setExpected(input);
			return act;
		}
	}

	/*
	 * public String handle(AgendaDialogueNode node) { String response = "";
	 * stack.remove(node);
	 *
	 * // Add children to stack if (node.hasChildren()) { // Add node response if
	 * (!node.getResponses().isEmpty()) { response =
	 * response.concat(node.getResponses().get(0)); response =
	 * response.concat("\n"); } stack.addAll(0, node.getChildren());
	 * AgendaDialogueNode subNode = node.getChildren().get(0); response =
	 * response.concat(handle(subNode)); } else {
	 *
	 * // Return request message of next slot response =
	 * response.concat(next().getResponses().get(0)); } return response; }
	 */

	public void reset() {
		this.stack = this.generateStack(this.root);
		stack.printAll();
		this.state = new DialogueState();
	}

	public AgendaDialogueStack generateStack(AgendaDialogueNode root) {

		AgendaDialogueStack stack = new AgendaDialogueStack();
		stack.add(root);
		stack.addAll(root.getChildren());
		return stack;
	}

	private AgendaDialogueNode next() {
		return this.stack.get(0);
	}

	private boolean isFilled() {
		System.out.println("check if filled");
		for (Slot slot : goal.getDescendants())
			if (!slot.hasChildren())
				if (!this.state.slotValues.containsKey(slot.getName())) {
					System.out.println("key not contained: " + slot.getName());
					return false;
				}
		System.out.println("is filled");
		return true;

	}

	public boolean validate() {

		// Stack and root node are instantiated
		if (stack == null || root == null)
			return false;
		return true;
	}

	protected AgendaDialogueStack getStack() {
		return stack;
	}

	protected void setStack(AgendaDialogueStack stack) {
		this.stack = stack;
	}

	protected AgendaDialogueNode getRoot() {
		return root;
	}

	public void setRoot(AgendaDialogueNode root) {
		this.root = root;
	}

	public Frame getGoal() {
		return goal;
	}

	public void setGoal(Frame goal) {
		this.goal = goal;
	}

	@Override
	public boolean hasIntent(String intent) {
		for (AgendaDialogueNode node : root.getDescendants())
			if (node.hasIntent(intent))
				return true;
		return false;
	}

	@Override
	public DialogueAct handleDefault() {
		AgendaDialogueNode next = next();
		String intent = next.getIntent();
		ExpectedInputType type = next.getInputType();
		String response = next.getResponses().get(0);

		DialogueAct act = new DialogueAct();
		act.setMessage(response);
		ExpectedInput input = new ExpectedInput(type, intent);
		act.setExpected(input);
		return act;

	}

	@Override
	public Collection<String> getIntents() {
		// TODO Auto-generated method stub
		return null;
	}

}
