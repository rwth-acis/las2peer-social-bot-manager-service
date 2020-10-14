package i5.las2peer.services.socialBotManagerService.parser.creation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = ChitChatFunction.class, name = "ChitChat"),
	@JsonSubTypes.Type(value = AccessServiceFunction.class, name = "AccessService") })
@ApiModel(discriminator = "type", subTypes = { ChitChatFunction.class, AccessServiceFunction.class })
public abstract class Function {
    @ApiModelProperty(dataType = "string", allowableValues = "AccessService, ChitChat", value = "The function the bot should do", required = true, example = "AccessService")
    private FunctionType type;

    @ApiModelProperty(dataType = "string", value = "The intent to start the service access", required = true, example = "add_pet")
    private String intent;

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

}
