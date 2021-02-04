package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

public interface Repeatable {

	public int size();
	
	public int getMinItems();
	
	public default int getNeeded() {
		if(size() >= getMinItems())
			return 0;
		return getMinItems() - size();
	}
}
