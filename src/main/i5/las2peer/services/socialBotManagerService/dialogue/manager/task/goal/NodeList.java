package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class NodeList extends ArrayList<Node> {

    private static final long serialVersionUID = 1L;

    public NodeList(Node node) {
	this.add(node);
    }

    List<Fillable> getFilledValues() {
	List<Fillable> res = new ArrayList<Fillable>();
	for (Node node : this) {
	    if (node instanceof Fillable)
		if (node.isFilled())
		    res.add((Fillable) node);
	}
	return res;
    }

    List<Fillable> getFillableNodes() {
	List<Fillable> res = new ArrayList<Fillable>();
	for (Node node : this) {
	    if (node instanceof Fillable)
		res.add((Fillable) node);
	}
	return res;
    }

    public boolean contains(String name) {
	for (Fillable node : getFillableNodes()) {
	    Slot slot = node.getSlot();
	    if (slot.getName().equals(name) || slot.getInformIntent().equals(name)
		    || slot.getRequestIntent().equals(name) || slot.getConfirmIntent().equals(name)
		    || slot.getDenyIntent().equals(name))
		return true;
	}
	return false;
    }

    public Collection<String> getIntents() {
	Collection<String> intents = new ArrayList<String>();
	for (Fillable node : getFillableNodes()) {
	    Slot slot = node.getSlot();
	    intents.add(slot.getInformIntent());
	    intents.add(slot.getRequestIntent());
	    intents.add(slot.getConfirmIntent());
	    intents.add(slot.getDenyIntent());
	}
	return intents;
    }

    public Fillable get(String name) {
	for (Fillable node : getFillableNodes()) {
	    Slot slot = node.getSlot();
	    if (name.contentEquals(slot.getName()) || name.contentEquals(slot.getInformIntent())
		    || name.contentEquals(slot.getRequestIntent()) || name.contentEquals(slot.getConfirmIntent())
		    || name.contentEquals(slot.getDenyIntent()))
		return node;
	}
	return null;
    }

    public void print() {
	for (Fillable node : this.getFillableNodes()) {
	    System.out.println(node.getSlot().getName());
	}
    }

}
