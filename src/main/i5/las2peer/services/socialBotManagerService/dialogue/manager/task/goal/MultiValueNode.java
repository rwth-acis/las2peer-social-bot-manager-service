package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class MultiValueNode extends Node {

    private Slot slot;
    private List<String> values;
    private boolean confirmed;

    public MultiValueNode(Slot slot) {

	assert slot != null : "slot is null";

	this.slot = slot;
	this.values = new ArrayList<String>();
	this.confirmed = false;

	invariant();
    }

    @Override
    public void fill(String value) {

	assert value != null : "value parameter is null";
	invariant();
	assert this.slot.validate(value) : "value '" + value + "' is not valid for slot " + this.slot.getName();

	this.values.add(value);

    }

    public void confirm() {

	invariant();
	assert this.isFilled() : "node value not filled yet";

	this.confirmed = true;

    }

    @Override
    public void clear() {
	invariant();

	this.values = new ArrayList<String>();
	this.confirmed = false;

    }

    @Override
    public boolean isFilled() {
	invariant();
	return !this.values.isEmpty();
    }

    @Override
    public boolean isReady() {
	invariant();
	return this.isFilled();
    }

    @Override
    public boolean isConfirmed() {
	invariant();
	return confirmed;
    }

    @Override
    public void setConfirmed(boolean confirmed) {
	this.confirmed = confirmed;
    }

    @Override
    public Slot getSlot() {
	return slot;
    }

    @Override
    public void setSlot(Slot slot) {
	this.slot = slot;
    }

    public List<String> getValues() {
	return values;
    }

    public void setValue(List<String> values) {
	this.values = values;
    }

    @Override
    public void invariant() {
	assert this.slot != null : "slot of value node is null";
	assert this.values != null : "values list is null";
	if (this.isFilled())
	    for (String value : this.values) {
		assert this.slot.validate(value) : "slot " + this.slot.getName() + " filled with invalid value "
			+ value;
	    }
    }

}
