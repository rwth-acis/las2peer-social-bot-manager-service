package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public interface Fillable {

    public void fill(String value);

    public boolean validate(String value);

    public void confirm();

    public void clear();

    public String getValue();

    public Slot getSlot();

    public boolean isFilled();

    public boolean isReady();

    public boolean isFull();

    public boolean isConfirmed();

    public default String getName() {
	return getSlot().getName();
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
