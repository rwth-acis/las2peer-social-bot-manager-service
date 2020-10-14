package i5.las2peer.services.socialBotManagerService.parser.creation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import i5.las2peer.services.socialBotManagerService.chat.ChatService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = SlackMessenger.class, name = "Slack"),
	@JsonSubTypes.Type(value = TelegramMessenger.class, name = "Telegram") })
@ApiModel(discriminator = "type", subTypes = { SlackMessenger.class, TelegramMessenger.class })
public abstract class Messenger {

    @ApiModelProperty(dataType = "string", allowableValues = "Slack, Telegram", value = "The messenger application you want to use.", required = true, example = "Slack")
    private ChatService type;

    public ChatService getType() {
	return type;
    }

    public void setType(ChatService type) {
	this.type = type;
    }

    @Override
    public String toString() {
	return "Messenger [type=" + type + "]";
    }

}
