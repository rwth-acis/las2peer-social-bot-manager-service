package i5.las2peer.services.socialBotManagerService.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;
import i5.las2peer.services.socialBotManagerService.model.BotModelNodeAttribute;

/**
 * Stores Bot Meta Model information
 *
 */
public class BotModelInfo {

	Map<String, Map<String, String>> attrIds;
	
	public BotModelInfo() {
		this.attrIds = new HashMap<>();
		this.addAttrId("Incoming Message", "Intent Keyword", "869849f65db9be737e99bd24");
		this.addAttrId("Incoming Message", "NLU ID", "e7d374a1e2d32c5e67bc68cc");
	}
	
	public void parse(BotModel model) {
		
		Collection<BotModelNode> nodes = model.getNodes().values();
		for(BotModelNode node :nodes) {
			String nodeName = node.getType();
			for(BotModelNodeAttribute attr :node.getAttributes().values()) {
				String attrId = attr.getId();
				String attrName = "";
				if(attr.getValue() != null && attr.getValue().getValue() != null) 
					attrName = attr.getValue().getValue();
				this.addAttrId(nodeName, attrName, attrId);
				System.out.println("add attr: " + nodeName + " " + attrName + " " + attrId);
			}
		}				
	}
	
		
	public String getAttrId(String elementName, String attributeName) {
		
		if(!this.attrIds.containsKey(elementName))
			return null;
		
		Map<String, String> ids = attrIds.get(elementName);		
		return ids.get(attributeName);
	}
	
	public void addAttrId(String elementName, String attributeName, String id) {
		if(!this.attrIds.containsKey(elementName))
			this.attrIds.put(elementName, new HashMap<>());
		
		Map<String, String> ids = attrIds.get(elementName);
		ids.put(attributeName, id);
	}
}
