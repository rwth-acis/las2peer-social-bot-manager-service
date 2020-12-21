package i5.las2peer.services.socialBotManagerService.parser.creation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = ChitChatFunction.class, name = "ChitChat"),
		@JsonSubTypes.Type(value = AccessServiceFunction.class, name = "AccessService"),
		@JsonSubTypes.Type(value = Notification.class, name = "Notification") })
@ApiModel(discriminator = "type", subTypes = { ChitChatFunction.class, AccessServiceFunction.class,
		Notification.class })
public abstract class Function {
	@ApiModelProperty(dataType = "string", allowableValues = "AccessService, ChitChat, Notification", required = true, value = "Which function should your bot do? \n*AccessService*: The bot lets the user access a web service. \n"
			+ "*ChitChat*: The bot has a casual conversation with the user."
			+ "*Notification*: The bot sends a notification when a service was accessed.")
	private FunctionType type;

	@ApiModelProperty(dataType = "string", value = "The intent to start the service access", required = true, example = "add_pet")
	private String intent;
	
	private String name;

	public FunctionType getType() {
		return type;
	}

	public void setType(FunctionType type) {
		this.type = type;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}
