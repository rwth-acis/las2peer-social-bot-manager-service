package i5.las2peer.services.socialBotManagerService.parser.creation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FunctionType {
    @JsonProperty("AccessService")
    SERVICE_ACCESS, @JsonProperty("ChitChat")
    CHIT_CHAT, @JsonProperty("Notification")
    NOTIFICATION,
}
