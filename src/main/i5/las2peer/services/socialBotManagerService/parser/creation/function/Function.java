package i5.las2peer.services.socialBotManagerService.parser.creation.function;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import i5.las2peer.services.socialBotManagerService.parser.creation.Notification;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = ChitChatFunction.class, name = "Chat Function"),
		@JsonSubTypes.Type(value = AccessServiceFunction.class, name = "AccessService")})
@ApiModel(discriminator = "type", subTypes = { ChitChatFunction.class, AccessServiceFunction.class,
		Notification.class })
public abstract class Function {
	@ApiModelProperty(dataType = "string", allowableValues = "AccessService, Chat Function", required = true, value = "Which function should your bot do? \n*AccessService*: The bot lets the user access a web service. \n"
			+ "*Chat Function*: The bot has a casual conversation with the user.")
	private FunctionType type;
	
	public FunctionType getType() {
		return type;
	}

	public void setType(FunctionType type) {
		this.type = type;
	}

}
