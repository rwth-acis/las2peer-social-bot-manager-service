package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;

public interface Fillable extends Slotable {

	public void fill(String value);

	public boolean validate(String value);

	public void confirm();

	public void clear();

	public String getValue();

	public boolean isFilled();

	public boolean isReady();

	public boolean isFull();

	public boolean isConfirmed();

	public default boolean hasEntity(String entityName) {

		if (getSlot().getEntity() == null)
			return false;

		return getSlot().getEntity().equalsIgnoreCase(entityName);
	}
	
	public default boolean hasFrameGeneratedEnum() {
		
		ServiceFunctionAttribute attr = this.getSlot().getParameter();
		if(attr == null)
			return false;
		
		if(attr.getRetrieveFunction() != null && attr.getRetrieveFunction().hasFrameGeneratedAttribute())
			return true;
		
		return false;
		
	}

}
