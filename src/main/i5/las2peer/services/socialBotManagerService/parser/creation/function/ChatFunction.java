package i5.las2peer.services.socialBotManagerService.parser.creation.function;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.parser.creation.Message;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = CreatorFunction.class, value = "Chat Function")
public class ChatFunction extends CreatorFunction{

	@ApiModelProperty(required = true)
	private List<Message> messages;

	public ChatFunction() {
		
	}
	
	public ChatFunction(String intent, String... responses) {
		
		this();
		this.setType(FunctionType.CHIT_CHAT);
		this.messages = new ArrayList<>();
		for (String response : responses) {
			Message message = new Message();
			message.setIntent(intent);
			message.setResponse(response);
			this.messages.add(message);
		}

	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	@Override
	public String toString() {
		return "ChatFunction [messages=" + messages + "]";
	}

}
