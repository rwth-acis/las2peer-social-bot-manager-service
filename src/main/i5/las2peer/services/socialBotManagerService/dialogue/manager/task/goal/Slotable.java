package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public interface Slotable {

    public Slot getSlot();

    public default String getName() {
	return getSlot().getName();
    }

    public default String getAPIName() {
	return getSlot().getAPIName();
    }

    public default String getDisplayName() {
	return getSlot().getDisplayName();
    }

    public default String getInformIntent() {
	return "inform_" + getName();
    }

    public default String getRequestIntent() {
	return "request_" + getName();
    }

    public default String getConfirmIntent() {
	return "confirm_" + getName();
    }

    public default String getDenyIntent() {
	return "deny_" + getName();
    }

}
