package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class ValueNode extends Node {

    private Slot slot;
    private String value;
    private boolean confirmed;

    public ValueNode(Slot slot) {

	assert slot != null : "slot parameter is null";

	this.slot = slot;
	this.value = null;
	this.confirmed = false;

	invariant();
    }

    @Override
    public void fill(String value) {

	assert value != null : "value parameter is null";
	invariant();
	assert this.slot.validate(value) : "value '" + value + "' is not valid for slot " + this.slot.getName();

	this.value = value;

    }

    public void confirm() {

	invariant();
	assert this.isFilled() : "node value not filled yet";
	assert this.value.contentEquals(value) : "value confirmed that was not filled yet";

	this.confirmed = true;

    }

    @Override
    public void clear() {
	invariant();

	this.value = null;
	this.confirmed = false;

    }

    @Override
    public boolean isFilled() {
	invariant();
	return this.value != null && !(this.value.contentEquals(""));
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

    @Override
    public String getValue() {
	return value;
    }

    @Override
    public void setValue(String value) {
	this.value = value;
    }

    @Override
    public void invariant() {
	assert this.slot != null : "slot of value node is null";
	if (this.isFilled())
	    assert this.slot.validate(this.getValue()) : "slot " + this.slot.getName() + " filled with invalid value "
		    + this.value;
    }

}
