package i5.las2peer.services.socialBotManagerService.parser.creation.function;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = ChatFunction.class, name = "Chat Function"),
		@JsonSubTypes.Type(value = ServiceAccessFunction.class, name = "Service Access")})
@ApiModel(discriminator = "type", subTypes = { ChatFunction.class, ServiceAccessFunction.class })
public class CreatorFunction {

	@ApiModelProperty(dataType = "string", allowableValues = "Service Access, Chat Function", required = true, value = "Which function should your bot do? \n*AccessService*: The bot lets the user access a web service. \n"
			+ "*Chat Function*: The bot has a casual conversation with the user.")
	private FunctionType type;
	
	public FunctionType getType() {
		return type;
	}

	public void setType(FunctionType type) {
		this.type = type;
	}
	
}
