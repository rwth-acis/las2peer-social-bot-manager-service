package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class AgendaDialogueManager extends AbstractDialogueManager {

    AgendaDialogueStack stack;
    AgendaDialogueNode root;
    DialogueState state;

    public AgendaDialogueManager() {
	
    }

    @Override
    public String handle(Intent semantic) {

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
			stack.addAll(node.getChildren());
		    }
		    System.out.println(response);
		    return response;
		}
		
		 if (node.hasChildren()) {
			stack.remove(node);
			stack.addAll(node.getChildren());
		  }
		
		// Entity as Slot value
		if (semantic.getEntity(node.getEntity()) != null) {
		    this.state.addSlotValue(node.getIntent(), semantic.getEntity(node.getEntity()).getValue());
		    System.out.println("Slot received: " + semantic.getEntity(node.getEntity()).getValue());
		    stack.remove(node);
		}
	    }
	}
	String response = this.next().getResponses().get(0).concat(stack.toString());
	System.out.println(response);
	return response;

    }

    public void reset() {
	this.stack = this.generateStack(this.root);
	this.state = new DialogueState();
    }

    public AgendaDialogueStack generateStack(AgendaDialogueNode root) {

	AgendaDialogueStack stack = new AgendaDialogueStack();
	stack.addAll(root.getChildren());
	return stack;
    }

    private AgendaDialogueNode next() {
	AgendaDialogueNode node = this.stack.firstElement();
	return this.stack.firstElement();
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

}
