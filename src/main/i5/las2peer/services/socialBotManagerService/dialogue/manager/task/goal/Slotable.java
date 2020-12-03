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
		return getSlot().getInformIntent();
	}

	public default String getRequestIntent() {
		return getSlot().getRequestIntent();
	}

	public default String getConfirmIntent() {
		return getSlot().getConfirmIntent();
	}
	
	public default String getDenyIntent() {
		return getSlot().getDenyIntent();
	}
	
	public default String getReqConfProceed() {
		return getSlot().getReqConfIntent();
	}



}
