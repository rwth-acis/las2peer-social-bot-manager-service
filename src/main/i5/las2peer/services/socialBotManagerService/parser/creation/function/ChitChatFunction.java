package i5.las2peer.services.socialBotManagerService.parser.creation.function;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import i5.las2peer.services.socialBotManagerService.parser.creation.Message;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(parent = Function.class, value = "Chat Function")
public class ChitChatFunction extends Function {

	@ApiModelProperty(required = true)
	private List<Message> messages;

	public ChitChatFunction(String intent, String... responses) {
		this();
		this.messages = new ArrayList<>();
		for (String response : responses) {
			Message message = new Message();
			message.setIntent(intent);
			message.setResponse(response);
			this.messages.add(message);
		}
				
	}

	public ChitChatFunction() {
		this.setType(FunctionType.CHIT_CHAT);
	}

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
