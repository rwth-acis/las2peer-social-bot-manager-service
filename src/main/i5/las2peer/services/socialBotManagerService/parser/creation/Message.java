package i5.las2peer.services.socialBotManagerService.parser.creation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class Message {

	@ApiModelProperty(dataType = "string", value = "The intent of the user message", required = true, example = "greet")
	private String intent;

	@ApiModelProperty(dataType = "string", value = "The answer that the bot should give", required = true, example = "Hi, i am a bot")
	private String response;

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String message) {
		this.response = message;
	}

	@Override
	public String toString() {
		return "Message [intent=" + intent + ", response=" + response + "]";
	}

}
