package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

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
	public synchronized String handle(Intent semantic) {

		System.out.println("ADM: handle intent: " + semantic.getKeyword() + " entity: " + semantic.getEntities());
		String intent = semantic.getKeyword();
		Iterator<AgendaDialogueNode> iterator = stack.iterator();
		while (iterator.hasNext()) {
			AgendaDialogueNode node = iterator.next();
			if (node.hasIntent(intent)) {
				stack.pushAll(node.getChildren());
				// Chat Node
				if (node.isPassive()) {
					ArrayList<String> responses = node.getResponses();
					String response = responses.get(new Random().nextInt(responses.size()));
					if (node.hasChildren()) {
						stack.remove(node);
						stack.addAll(0, node.getChildren());
					}
					System.out.println(response);
					return response;
				}

				if (node.hasChildren()) {
					stack.remove(node);
					stack.addAll(0, node.getChildren());
				}

				// Entity as Slot value
				if (semantic.getEntity(node.getEntity()) != null) {
					this.state.addSlotValue(node.getIntent(), semantic.getEntity(node.getEntity()).getValue());
					System.out.println("Slot received: " + semantic.getEntity(node.getEntity()).getValue());
					stack.remove(node);
					if (isFilled()) {

						String response = this.goalMessage.concat(" : ");
						System.out.println("values: " + this.state.slotValues.size());
						for (String value : this.state.slotValues.values()) {
							System.out.println(value);
							response = response.concat(", ").concat(value);
						}
						this.reset();
						return response;
					}
				}
			}
		}
		String response = this.next().getResponses().get(0).concat(stack.toString());
		System.out.println(response);
		this.stack.printAll();
		return response;

	}

	public void reset() {
		this.stack = this.generateStack(this.root);
		stack.printAll();
		this.state = new DialogueState();
	}

	public AgendaDialogueStack generateStack(AgendaDialogueNode root) {

		AgendaDialogueStack stack = new AgendaDialogueStack();
		stack.addAll(root.getChildren());
		return stack;
	}

	private AgendaDialogueNode next() {
		for (AgendaDialogueNode node : this.stack) {
			if (!node.isPassive() && !node.hasChildren())
				return node;
		}
		return this.stack.get(1);
	}

	private boolean isFilled() {
		for (Slot slot : goal.getSlots().values()) {
			if (!this.state.slotValues.containsKey(slot.getName()))
				return false;
		}
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

	protected void setRoot(AgendaDialogueNode root) {
		this.root = root;
	}

	public Frame getGoal() {
		return goal;
	}

	public void setGoal(Frame goal) {
		this.goal = goal;
	}

}
