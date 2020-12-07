package i5.las2peer.services.socialBotManagerService.parser.creation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = las2peerService.class, name = "las2peer"),
		@JsonSubTypes.Type(value = OpenAPIService.class, name = "OpenAPI") })
@ApiModel(discriminator = "type", subTypes = { las2peerService.class, OpenAPIService.class })
public class ServiceType {
	@ApiModelProperty(dataType = "string", allowableValues = "las2peer, OpenAPI", required = true, value = "Which type of service the bot should access?")
	ServiceAccessType type;

	public ServiceAccessType getType() {
		return type;
	}

	public void setType(ServiceAccessType type) {
		this.type = type;
	}
}
