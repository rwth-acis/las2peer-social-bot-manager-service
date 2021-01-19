package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class SlotList extends ArrayList<Slot> {

	private static final long serialVersionUID = 2835382998066194672L;

	public boolean contains(String name) {

		for (Slot slot : this) {
			if (slot.getID().contains(name) || slot.getName().equals(name) || slot.getIntents().contains(name))
				return true;
		}
		return false;
	}

	public Slot get(String name) {

		// slot name
		for (Slot slot : this) {
			if (slot.getName().equalsIgnoreCase(name) || slot.getIntents().contains(name))
				return slot;
		}

		// parameter name
		for (Slot slot : this) {
			if (slot.hasParameter())
				if (slot.getParameter().getIdName().equalsIgnoreCase(name))
					return slot;
		}

		// api name
		for (Slot slot : this) {
			if (name.equalsIgnoreCase(slot.getAPIName()))
				return slot;
		}

		return null;
	}

	public Collection<String> getIntents() {

		Collection<String> intents = new ArrayList<String>();
		for (Slot slot : this) {
			intents.addAll(slot.getIntents());
		}
		return intents;
	}

	@Override
	public String toString() {

		String res = "SlotSet [";
		for (Slot slot : this) {
			res = res.concat(slot.toString());
		}
		res = res.concat("]");
		return res;
	}

}
