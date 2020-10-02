package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class RepetitionNode extends Node {

    private Slot slot;
    private List<Node> valueChildren;
    boolean confirmed;

    public void extend() {

	invariant();

	Node node = NodeFactory.create(slot);
	this.valueChildren.add(node);

    }

    public void confirm() {
	invariant();
	assert this.isFilled() : "node value not filled yet";

	this.confirmed = true;

    }

    @Override
    public void clear() {
	invariant();

	this.valueChildren = new ArrayList<Node>();
	extend();
	this.confirmed = false;

    }

    @Override
    public boolean isFilled() {
	invariant();
	if (this.valueChildren.isEmpty())
	    return false;
	return true;
    }

    @Override
    public boolean isReady() {
	invariant();
	if (!this.isFilled())
	    return false;

	for (Node node : this.valueChildren) {
	    if (!node.isReady()) {
		return false;
	    }
	}

	return true;
    }

    @Override
    public boolean isConfirmed() {
	return confirmed;
    }

    @Override
    public void setConfirmed(boolean confirmed) {
	this.confirmed = confirmed;
    }

    public List<Node> getValueChildren() {
	return valueChildren;
    }

    public void setValueChildren(List<Node> valueChildren) {
	this.valueChildren = valueChildren;
    }

    @Override
    public Slot getSlot() {
	return slot;
    }

    @Override
    public void setSlot(Slot slot) {
	this.slot = slot;
    }

    @Override
    public void invariant() {
	assert this.slot != null : "slot is null";
	assert this.valueChildren != null : "valueChildren are null";
	for (Node node : this.valueChildren) {
	    node.invariant();
	}
    }


}
