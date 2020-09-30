package i5.las2peer.services.socialBotManagerService.parser.creation;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = Function.class, value = "ChitChat")
public class ChitChatFunction {

	@ApiModelProperty(dataType = "array", value = "The message of the user", required = true)
	private List<Message> messages;

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

}
