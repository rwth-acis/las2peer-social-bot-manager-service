package i5.las2peer.services.socialBotManagerService.dialogue.task;

import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInputType;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class DialogueGoal {

    Frame frame;
    Map<Slot, String> values;

    public DialogueGoal(Frame frame) {
	this.values = new HashMap<Slot, String>();
	this.frame = frame;
    }

    /**
     * @return true if all required slots are filled.
     */
    public boolean isReady() {

	assert this.frame != null : "frame of dialogue goal is null";
	assert this.values != null : "slot value map of dialogue goal is null";

	for (Slot slot : frame.getRequired()) {
	    
	    if (!this.values.containsKey(slot)) {
		System.out.println("key not contained: " + slot.getName());
		return false;
	    }
	}
	return true;
    }

    /**
     * @return true if all slots are filled.
     */
    public boolean isFull() {

	assert this.frame != null : "frame of dialogue goal is null";
	assert this.values != null : "slot value map of dialogue goal is null";

	for (Slot slot : frame.getDescendants()) {
	    if (!this.values.containsKey(slot)) {
		System.out.println("key not contained: " + slot.getName());
		return false;
	    }
	}
	return true;
    }

    /**
     * @param slot
     * @param value
     * @return true if slot was filled. false if value does not match the slot.
     */
    public boolean fill(Slot slot, String value) {

	assert this.values != null : "slot value map of dialogue goal is null";
	assert slot != null : "slot parameter is null";
	assert value != null : "value parameter is null";

	// check input type
	ExpectedInputType type = slot.getInputType();
	if (!type.validate(value)) {
	    System.out.println("input does not match type: " + type);
	    return false;
	}

	// fill slot
	if (this.values.containsKey(slot)) {
	    System.out.println("override slot " + slot.getName() + " value: " + values.get(slot) + " with " + value);
	}
	this.values.put(slot, value);
	System.out.println("fill slot value " + slot.getName() + " " + value);

	return true;
    }

    /**
     * @param slot
     * @return true if slot was deleted. false if slot was not filled.
     */
    public boolean delete(Slot slot) {

	assert this.values != null : "slot value map of dialogue goal is null";
	assert slot != null : "slot parameter is null";

	if (!this.values.containsKey(slot)) {
	    System.out.println("slot " + slot.getName() + " is not contained");
	    return false;
	}

	this.values.remove(slot);
	return true;
    }

    /**
     * @param slot
     * @return true if slot is part of this goal.
     */
    public boolean contains(Slot slot) {
	
	assert this.frame != null : "frame of dialogue goal is null";
	if(this.frame.getDescendants() == null)
	    return false;
	return this.frame.getDescendants().contains(slot);
    }
    
    public boolean contains(String name) {
	
	assert this.frame != null : "frame of dialogue goal is null";	
	if(this.frame.getDescendants() == null)
	    return false;	
	return frame.getDescendants().contains(name);
    }

    /**
     * @param slot
     * @return true if slot is filled.
     */
    public boolean isFilled(Slot slot) {
	
	assert this.frame != null : "frame of dialogue goal is null";
	assert this.values != null : "slot value map of dialogue goal is null";
	assert slot != null : "slot parameter is null";
	assert this.contains(slot) : "slot is not part of this goal";
		
	return this.values.containsKey(slot);
    }
    
    public Slot get(String name) {
	
	assert this.frame != null : "frame of dialogue goal is null";
	assert this.contains(name): "slot is not contained in frame";
	
	Slot slot = frame.getSlot(name);
	return slot;
    }
    
    /**
     * @return a slot that is not filled yet.
     */
    public Slot next() {

	for (Slot slot : frame.getRequired()) {
	    if (!this.values.containsKey(slot)) {
		return slot;
	    }
	}
	
	for (Slot slot : frame.getDescendants()) {
	    if (!this.values.containsKey(slot)) {
		return slot;
	    }
	}
	
	return null;
    }

}
