package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

public class AgendaDialogueStack extends Stack<AgendaDialogueNode> {

	private static final long serialVersionUID = 1L;

	public void pushAll(Collection<AgendaDialogueNode> items) {
		Iterator<AgendaDialogueNode> iterator = items.iterator();
		while (iterator.hasNext()) {
			this.push(iterator.next());
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
