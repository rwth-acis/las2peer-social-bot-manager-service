package i5.las2peer.services.socialBotManagerService.parser.openapi;

public interface RepeatingNode {

	public int size();
	
	public int getMinItems();
	
	public default int getNeeded() {
		if(size() >= getMinItems())
			return 0;
		return getMinItems() - size();
	}
}
