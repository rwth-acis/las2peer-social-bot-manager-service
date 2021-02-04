package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class MultiValueNode extends Node implements Fillable, Repeatable {

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
		assert this.slot.validate(value) : "value '" + value + "' is not valid for slot " + this.getAPIName();

		this.values.add(value);

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

		this.values = new ArrayList<String>();
		this.confirmed = false;

	}

	@Override
	public boolean isFilled() {
		invariant();

		if (this.size() < this.getMinItems())
			return false;

		return !this.values.isEmpty();
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

	public List<String> getValues() {
		return values;
	}

	public void setValue(List<String> values) {
		this.values = values;
	}

	@Override
	public String getValue() {
		String res = "";
		res = res.concat(this.getValues().get(0));
		if (this.getValues().size() > 0) {
			for (String value : this.values.subList(1, this.values.size())) {
				res = res.concat(", ").concat(value);
			}
		}
		return res;

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
	public int size() {
		return this.values.size();
	}

	@Override
	public int getMinItems() {
		if (this.getSlot().getParameter() == null)
			return 0;
		return this.getSlot().getParameter().getMinItems();
	}

	@Override
	protected void invariant() {
		assert this.slot != null : "slot of value node is null";
		assert this.values != null : "values list is null";

		if (!this.slot.hasDynamicEnums())
			for (String value : this.values) {
				assert this.slot.validate(value) : "slot " + this.slot.getName() + " filled with invalid value "
						+ value;
			}
	}

	@Override
	public JSONObject toBodyJSON() {
		invariant();

		if (!this.isBodyAttribute())
			return null;

		if (!this.isFilled())
			return null;

		JSONObject res = new JSONObject();
		JSONArray ar = new JSONArray();
		for (String value : this.values) {
			ar.add(value);
		}
		res.put(this.getAPIName(), ar);
		return res;
	}

}
