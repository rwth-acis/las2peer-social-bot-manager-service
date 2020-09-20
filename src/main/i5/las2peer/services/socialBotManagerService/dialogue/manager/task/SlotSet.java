package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class SlotSet extends ArrayList<Slot> {

    private static final long serialVersionUID = 2835382998066194672L;

    public boolean contains(String name) {
	for (Slot slot : this) {
	    if (slot.getName().equals(name) || slot.getInformIntent().equals(name)
		    || slot.getRequestIntent().equals(name) || slot.getConfirmIntent().equals(name)
		    || slot.getDenyIntent().equals(name))
		return true;
	}
	return false;
    }

    public Collection<String> getIntents() {
	Collection<String> intents = new ArrayList<String>();
	for (Slot slot : this) {
	    intents.add(slot.getInformIntent());
	    intents.add(slot.getRequestIntent());
	    intents.add(slot.getConfirmIntent());
	    intents.add(slot.getDenyIntent());
	}
	return intents;
    }

    public Slot get(String name) {
	for (Slot slot : this) {
	    if (name.contentEquals(slot.getName()) || name.contentEquals(slot.getInformIntent())
		    || name.contentEquals(slot.getRequestIntent()) || name.contentEquals(slot.getConfirmIntent())
		    || name.contentEquals(slot.getDenyIntent()))
		return slot;
	}
	return null;
    }

}
