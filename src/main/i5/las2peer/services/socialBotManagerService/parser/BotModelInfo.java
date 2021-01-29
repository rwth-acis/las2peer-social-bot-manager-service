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
		this.addAttrId("Incoming Message", "NLU ID", "698ef7b4664ffdb9010833d0");
		this.addAttrId("Incoming Message", "Response Message", "e7d374a1e2d32c5e67bc68cc");	
		
		this.addAttrId("Chat Response", "Message", "a5844a8bde0637231a942b69");	
		
		this.addAttrId("Instance", "Name", "4bfaada53679f7e979bcc1e6");
		this.addAttrId("Instance", "Address", "a7c8d03fd57a143943da1f48");		
		this.addAttrId("Instance", "Environment Separator", "3640924305c3d50b3a2de10d");
		
		this.addAttrId("Bot", "Name", "80f33463988c54141b8c4dd8");		
		this.addAttrId("Bot", "Description", "567bf6800fb85cf28b824379");
		
		this.addAttrId("Knowledge", "Type", "69e4efca72f32b65d1da3612");
		this.addAttrId("Knowledge", "Name", "dbcf8b46f8cd76f445b9dea4");
		this.addAttrId("Knowledge", "URL", "c051de6af456e91e52c0c465");
		
		this.addAttrId("Messenger", "Authentication Token", "4d07dc2fa7f97c4ef5834078");
		this.addAttrId("Messenger", "Name", "fac7a2b850919e041f655919");
		this.addAttrId("Messenger", "Messenger Type", "646c1466c4bf34e8267c4f26");
		
		this.addAttrId("Frame", "Intent Keyword", "37f906be663f9c0b23ffdb43");
		this.addAttrId("Frame", "Operation Name", "479044e86182aad50f1ad086");
		this.addAttrId("Frame", "Operation Description", "6d109dd3edd809143553ed38");
		this.addAttrId("Frame", "Success Response", "66419c40734bdf81206ede36");
		
		this.addAttrId("Bot Action", "Function Name", "c436dc0e764a63371820e857");
		this.addAttrId("Bot Action", "Service Alias", "bc1335cdf59dedb856e635be");
		this.addAttrId("Bot Action", "Action Type", "5529bde8544426cd5580efc1");
		
		
		this.addAttrId("Action Parameter", "Name", "68f226ab6d61c452f8834cae");
		this.addAttrId("Action Parameter", "Content", "23711db691227bba16e9d9c3");
		this.addAttrId("Action Parameter", "Parameter Type", "c64fe29fae8daad81617c282");
		
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
