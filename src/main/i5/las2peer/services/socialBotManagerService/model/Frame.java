package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import i5.las2peer.services.socialBotManagerService.dialogue.Command;
import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.SlotList;

/**
 * Frame generates a dialogue to trigger a Bot Action
 *
 */
public class Frame implements MessengerElement, Menuable {

	/**
	 * UUID of this element
	 */
	private final String id;
	
	private String name;

	/**
	 * intent keyword to trigger this element
	 */
	private String intent;

	private String message;

	private String successResponse;
	
	private String errorResponse;

	private String file;

	/**
	 * slots that this frame consists of (tree structure)
	 */
	private Map<String, Slot> slots;

	
	/**
	 *  Service Function that is triggered by this frame
	 */
	private ServiceFunction serviceFunction;

	/**
	 * Events that are generated when completing this frame
	 */
	private Collection<ServiceEvent> events;
	
	/**
	 * Ids of attributes (corresponding to further service functions) that are filled by this Frame
	 */
	private Map<String, String> attrIds;

	public Frame() {
		this.id = UUID.randomUUID().toString();
		this.slots = new HashMap<>();
		this.events = new HashSet<>();
		this.attrIds = new HashMap<>();
	}

	public Frame(String id) {
		this.id = id;
		this.slots = new HashMap<String, Slot>();
		this.events = new HashSet<ServiceEvent>();		
	}

	public Frame(String id, String name, String intent, String message, ServiceFunction serviceFunction, Map<String, Slot> slots) {
		this(id);
		this.name = name;
		this.intent = intent;
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
		if (this.name != null && !this.name.contentEquals(""))
			return name;
		return this.getIntent();
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

	/**
	 * @return the command to initiate a conversation for this frame
	 * 
	 */
	@Override
	public Command getCommand() {

		invariant();

		Command res = new Command();

		if (this.getIntent() != null)
			res.setIntent(this.getIntent());

		if (this.message != null)
			res.setDescription(this.message);

		if (this.getName() != null && !this.getName().contentEquals(""))
			res.setName(this.getName());
		else
			res.setName(this.getIntent());

		res.invariant();
		return res;

	}

	/**
	 * @return all intents related to this frame
	 */
	public List<String> getIntents() {

		List<String> res = new ArrayList<>();

		// Frame intents
		res.add(getIntent());
		res.add(getReqConfIntent());
		res.add(getReqConfIntent() + "_optional");

		// Slot intents
		List<Slot> slots = getDescendants();
		for (Slot slot : slots) {
			res.add(slot.getInformIntent());
		}

		return res;
	}

	@Override
	public String toString() {
		String res = "Frame - name: ".concat(this.getName()).concat(" intent: ").concat(this.getIntent())
				.concat("slots: ").concat(String.valueOf(this.slots.size()));
		for (Slot slot : getSlots().values())
			res = res.concat(slot.toString());
		return res;
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
		SlotList slots = getDescendants();
		return slots.get(name);
	}

	public SlotList getRequired() {
		SlotList res = new SlotList();
		for (Slot slot : this.slots.values())
			res.addAll(slot.getRequired());

		return res;
	}

	public SlotList getLeafs() {
		SlotList leafs = this.getDescendants();
		for (Slot slot : leafs)
			if (slot.hasChildren())
				leafs.remove(slot);

		return leafs;
	}

	public SlotList getDescendants() {
		SlotList desc = new SlotList();
		for (Slot slot : this.slots.values())
			desc.addAll(slot.getDescendants());

		return desc;
	}

	public SlotList getDescendants(Map<Slot, String> values) {
		SlotList desc = new SlotList();
		for (Slot slot : this.slots.values())
			desc.addAll(slot.getDescendants(values));

		return desc;
	}

	public SlotList getRequired(Map<Slot, String> values) {
		SlotList slots = this.getDescendants(values);
		SlotList res = new SlotList();
		for (Slot slot : slots)
			if (slot.isRequired())
				res.add(slot);
		return res;
	}

	public void setMessage(String value) {
		assert value != null : "value parameter is null";
		this.message = value;
	}

	public String getMessage() {
		if(this.message == null)
			return "";
		return this.message;
	}

	public void invariant() {
		assert this.slots != null : "frame has no slots";
		assert this.intent != null : "frame intent is null";
		assert this.serviceFunction != null: "frame has no service function";
		this.serviceFunction.invariant();
	}

	public String getSuccessResponse() {
		return successResponse;
	}

	public void setSuccessResponse(String response) {
		this.successResponse = response;
	}
	
	public String getErrorResponse() {
		return errorResponse;
	}

	public void setErrorResponse(String response) {
		this.errorResponse = response;
	}

	public boolean hasSuccessResponse() {
		return this.successResponse != null && !this.successResponse.contentEquals("");
	}
	
	public boolean hasErrorResponse() {
		return this.errorResponse != null && !this.errorResponse.contentEquals("");
	}
	
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public boolean hasServiceEvents() {
		return (events != null && !events.isEmpty());
	}

	public Collection<ServiceEvent> getServiceEvents() {
		return events;
	}

	public void addServiceEvent(ServiceEvent event) {
		events.add(event);
	}

	public Map<String, String> getFilledAttrIds() {
		return this.attrIds;
	}
	
	/**
	 * Register attributes of second level functions that are the same as first level function attributes.
	 * 
	 * @param slotName of filling attribute
	 * @param AttrId of to be filled attribute
	 */
	public void addFilledAttrId(String slotName, String attrId) {
		assert slotName != null;
		assert attrId != null;
		
		if(this.attrIds == null)
			this.attrIds = new HashMap<>();
		this.attrIds.put(slotName, attrId);
		
	}
	
	public boolean hasOptionalSlots() {
		for (Slot slot : this.getDescendants()) {
			if (!slot.isRequired())
				return true;
		}
		return false;
	}

	public Collection<InputType> getValueTypes() {
		Set<InputType> res = new HashSet<InputType>();
		for (Slot slot : this.getDescendants()) {
			if (slot.getInputType() != null)
				res.add(slot.getInputType());
		}
		return res;
	}

	@Override
	public String getIntentKeyword() {
		return this.intent;
	}
	
	@Override
	public boolean isOperation() {
		return true;
	}

	public String prettyPrint() {
		String res = "--- Frame: " + this.getName();
		for(Slot slot : this.slots.values()) {
			res = "\n" + res + slot.prettyPrint(0);
		}
		return res;
	}

}
