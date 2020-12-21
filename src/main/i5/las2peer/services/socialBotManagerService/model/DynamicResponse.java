package i5.las2peer.services.socialBotManagerService.model;

import i5.las2peer.services.socialBotManagerService.parser.openapi.ResponseParseMode;

public interface DynamicResponse {

	public ResponseParseMode getResponseParseMode();
	
	public ServiceFunction getResponseFunction();
	
	public String getResponseMessage();
	
	public String getActIntent();
	
	public default boolean hasDynamicResponse() {
		return getResponseFunction() != null;
	}
	
}
