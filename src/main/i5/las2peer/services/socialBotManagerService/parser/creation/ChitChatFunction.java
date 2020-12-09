package i5.las2peer.services.socialBotManagerService.parser.creation;

import java.util.List;

import io.swagger.annotations.ApiModel;

@ApiModel(parent = Function.class, value = "ChitChat")
public class ChitChatFunction extends Function {
		
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
