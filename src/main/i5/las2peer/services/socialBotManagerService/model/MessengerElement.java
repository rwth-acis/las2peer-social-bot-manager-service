package i5.las2peer.services.socialBotManagerService.model;

import java.util.Collection;
import java.util.HashSet;

public interface MessengerElement {

	/**
	 * @return intent keyword to activate this element
	 */
	public String getIntentKeyword();
	
	/**
	 * @return intent keyword and all keywords from follow up elements
	 */
	public default Collection<String> getNLUIntents() {
		Collection<String> res = new HashSet<>();
		res.add(getIntentKeyword());
		return res;
	}
	
}
