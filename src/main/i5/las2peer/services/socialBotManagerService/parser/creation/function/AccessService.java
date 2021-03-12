package i5.las2peer.services.socialBotManagerService.parser.creation.function;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "accesstype", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = Las2peerAccessService.class, name = "las2peer"),
		@JsonSubTypes.Type(value = OpenAPIAccessService.class, name = "OpenAPI") })
@ApiModel(discriminator = "accesstype", subTypes = { Las2peerAccessService.class, OpenAPIAccessService.class })
public class AccessService {
	@ApiModelProperty(dataType = "string", allowableValues = "las2peer, OpenAPI", required = true, value = "Which type of service the bot should access?")
	String accesstype;

	public String getAccesstype() {
		return accesstype;
	}

	public void setAccesstype(String accesstype) {
		this.accesstype = accesstype;
	}
}
