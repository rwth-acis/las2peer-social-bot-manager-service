package i5.las2peer.services.socialBotManagerService.model;

import java.util.Map;

import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ResponseParseMode;

/**
 * Generate a response message by a function call
 *
 */
public class GeneratorFunction {

	ResponseParseMode mode;
	ServiceFunction function;
	String key;

	public GeneratorFunction(ResponseParseMode mode, ServiceFunction function, String key) {
		super();
		this.mode = mode;
		this.function = function;
		this.key = key;
	}
	
	public String generate(String input, Map<String, String> parameters) {
	
		if(!this.hasFunction())
			return input;
		
		OpenAPIAction action = new OpenAPIAction(function, parameters);
		if(!action.validate())
			throw new IllegalStateException("Not all required parameters are filled");
		String response = action.execute();
		
		return input;
		
	}

	public boolean hasFunction() {
		return this.function != null;
	}

}
