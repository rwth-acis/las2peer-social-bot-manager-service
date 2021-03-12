package i5.las2peer.services.socialBotManagerService.parser.creation.function;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FunctionType {
	@JsonProperty("Service Access")
	SERVICE_ACCESS, 
	
	@JsonProperty("Chat Function")
	CHIT_CHAT, 
	
	@JsonProperty("Notification")
	NOTIFICATION,
}
