package i5.las2peer.services.socialBotManagerService.dialogue.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class DialogueState {
    
    Collection<Intent> acts = new ArrayList<Intent>();
    Map<String, String> slotValues = new HashMap<String, String>();
    
    public void addDialogueAct(Intent intent) {
	this.acts.add(intent);
    }
    
    public void addSlotValue(Slot slot, String value) {
	this.slotValues.put(slot.getName(), value);
    }
    
    public void addSlotValue(String slot, String value) {
	this.slotValues.put(slot, value);
    }
    
    public String getSlotValue(Slot slot) {
	if (this.slotValues.containsKey(slot.getName()))
		return slotValues.get(slot.getName());
	return null;
    }
        
}
