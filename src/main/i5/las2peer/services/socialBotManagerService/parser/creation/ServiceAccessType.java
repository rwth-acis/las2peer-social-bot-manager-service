package i5.las2peer.services.socialBotManagerService.parser.creation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ServiceAccessType {
	
    @JsonProperty("las2peer")
    las2peer, @JsonProperty("OpenAPI")
    OPENAPI, 
}
