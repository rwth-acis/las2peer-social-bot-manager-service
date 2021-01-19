package i5.las2peer.services.socialBotManagerService.parser.creation;

import java.util.List;

import i5.las2peer.services.socialBotManagerService.parser.creation.function.Function;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = Function.class, value = "ChitChat")
public class ChitChatFunction extends Function {

	@ApiModelProperty(required = true)
	private List<Message> messages;

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

    @Override
    public String toString() {
	return "ChitChatFunction [messages=" + messages + "]";
    }

}
