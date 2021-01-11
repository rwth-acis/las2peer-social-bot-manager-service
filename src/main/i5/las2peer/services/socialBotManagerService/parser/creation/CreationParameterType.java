package i5.las2peer.services.socialBotManagerService.parser.creation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CreationParameterType {
	@JsonProperty("Static Content")
	STATIC_CONTENT, @JsonProperty("Ask for it!")
	ASK_FOR_IT
}
