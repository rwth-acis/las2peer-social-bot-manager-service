package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ParameterType;
import net.minidev.json.JSONObject;

public class ValueNode extends Node implements Fillable {

	private Slot slot;
	private String value;
	private boolean confirmed;

	public ValueNode(Slot slot) {

		assert slot != null : "slot parameter is null";

		this.slot = slot;
		
		if(this.slot.getParameter().hasContent()) {
			this.value = this.slot.getParameter().getContent();
			this.confirmed = true;
		} else {
			this.value = null;
			this.confirmed = false;
		}

		invariant();
	}

	@Override
	public void fill(String value) {

		assert value != null : "value parameter is null";
		invariant();
		assert this.slot.validate(value) : "value '" + value + "' is not valid for slot " + this.slot.getName();

		this.value = value;

	}

	@Override
	public boolean validate(String value) {

		assert value != null : "value parameter is null";
		invariant();

		return this.slot.validate(value);
	}

	@Override
	public void confirm() {

		invariant();
		assert this.isFilled() : "node value not filled yet";

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
		if (!this.getSlot().isRequired())
			return true;

		return this.isFilled();
	}

	@Override
	public boolean isFull() {
		invariant();
		return this.isFilled();
	}

	public boolean isRequired() {
		invariant();
		return this.slot.isRequired();
	}

	@Override
	public boolean isConfirmed() {
		invariant();
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	@Override
	public Slot getSlot() {
		return slot;
	}

	public void setSlot(Slot slot) {
		this.slot = slot;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public Node next() {
		return this;
	}

	@Override
	public NodeList getAll() {
		return new NodeList(this);
	}

	@Override
	public void invariant() {
		assert this.slot != null : "slot of value node is null";
		if (this.value != null)
			assert this.slot.validate(this.getValue()) : "slot " + this.slot.getName() + " filled with invalid value "
					+ this.value;
	}

	@Override
	public JSONObject toJSON() {
		invariant();

		JSONObject res = new JSONObject();
		ParameterType type = this.getSlot().getParameter().getParameterType();
		if (type == ParameterType.BODY || type == ParameterType.CHILD || type == ParameterType.DISCRIMINATOR)
			res.put(this.getAPIName(), this.getValue());					
		
		return res;
	}

}
