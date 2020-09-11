package i5.las2peer.services.socialBotManagerService.dialogue.task;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

public class AgendaDialogueStack extends CopyOnWriteArrayList<AgendaDialogueNode> {

	private static final long serialVersionUID = 1L;

	public void pushAll(Collection<AgendaDialogueNode> items) {
		Iterator<AgendaDialogueNode> iterator = items.iterator();
		while (iterator.hasNext()) {
			this.add(iterator.next());
		}
	}
	
	public void printAll() {
		System.out.println("Stack: ");
		for(AgendaDialogueNode node: this) {
			System.out.println(node.getIntent());
		}
	}
	
	public String toString() {
	    String string = "";
	    for(AgendaDialogueNode node: this) {
		string.concat(node.getIntent()).concat(", ");
	    }
	    return string;	    
	}

}
