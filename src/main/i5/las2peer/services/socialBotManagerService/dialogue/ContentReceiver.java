package i5.las2peer.services.socialBotManagerService.dialogue;

import i5.las2peer.services.socialBotManagerService.model.GeneratorFunction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;

public class ContentReceiver {

	OpenAPIAction function;
	
	public ContentReceiver(GeneratorFunction function) {
		this.function = new OpenAPIAction(function);
				
	}

}
