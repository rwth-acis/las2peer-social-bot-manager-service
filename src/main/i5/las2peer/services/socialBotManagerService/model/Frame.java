package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.SlotSet;

public class Frame {

	private String name;

	private String intent;

	private String startMessage;

	private String endMessage;

	private ServiceFunction serviceFunction;

	private Map<String, Slot> slots;

	public Frame() {
		this.slots = new HashMap<String, Slot>();
	}

	public Frame(String name, String intent, String message, ServiceFunction serviceFunction, Map<String, Slot> slots) {
		super();
		this.name = name;
		this.intent = intent;
		this.startMessage = message;
		this.serviceFunction = serviceFunction;
		this.slots = slots;
	}

	public Map<String, Slot> getSlots() {
		return slots;
	}

	public void setSlots(Map<String, Slot> slots) {
		this.slots = slots;
	}

	public void addSlot(Slot slot) {
		this.slots.put(slot.getName(), slot);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public ServiceFunction getServiceFunction() {
		return serviceFunction;
	}

	public void setServiceFunction(ServiceFunction serviceFunction) {
		this.serviceFunction = serviceFunction;
	}

	public String getStartMessage() {
		return this.startMessage;
	}

	public void setStartMessage(String message) {
		this.startMessage = message;
	}

	@Override
	public String toString() {
		String res = "Frame - name: ".concat(this.getName()).concat(" intent: ").concat(this.getIntent())
				.concat(" start message: ").concat(this.getStartMessage()).concat("slots: ")
				.concat(String.valueOf(this.slots.size()));
		for (Slot slot : getSlots().values())
			res = res.concat(slot.toString());
		return res;
	}

	public String getEndMessage() {
		return endMessage;
	}

	public void setEndMessage(String endMessage) {
		this.endMessage = endMessage;
	}

	public String getReqConfIntent() {
		return ("reqconf_" + this.name);
	}

	public String getConfirmIntent() {
		return ("confirm_" + this.name);
	}

	public Map<String, Slot> getGoals() {
		Map<String, Slot> map = new HashMap<String, Slot>();
		for (Entry<String, Slot> entry : this.slots.entrySet())
			if (!entry.getValue().hasChildren())
				map.put(entry.getKey(), entry.getValue());
		return map;
	}

	public Slot getSlot(String name) {
		SlotSet slots = getDescendants();
		return slots.get(name);
	}

	public SlotSet getRequired() {
		SlotSet res = new SlotSet();
		for (Slot slot : this.slots.values())
			res.addAll(slot.getRequired());

		return res;
	}

	public SlotSet getLeafs() {
		SlotSet leafs = this.getDescendants();
		for (Slot slot : leafs)
			if (slot.hasChildren())
				leafs.remove(slot);

		return leafs;
	}

	public SlotSet getDescendants() {
		SlotSet desc = new SlotSet();
		for (Slot slot : this.slots.values())
			desc.addAll(slot.getDescendants());

		return desc;
	}

	public SlotSet getDescendants(Map<Slot, String> values) {
		SlotSet desc = new SlotSet();
		for (Slot slot : this.slots.values())
			desc.addAll(slot.getDescendants(values));

		return desc;
	}

	public SlotSet getRequired(Map<Slot, String> values) {
		SlotSet slots = this.getDescendants(values);
		SlotSet res = new SlotSet();
		for (Slot slot : slots)
			if (slot.isRequired())
				res.add(slot);
		return res;
	}

}
