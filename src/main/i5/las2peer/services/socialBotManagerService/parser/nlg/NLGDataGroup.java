package i5.las2peer.services.socialBotManagerService.parser.nlg;

import io.swagger.annotations.ApiModelProperty;

public class NLGDataGroup {

	@ApiModelProperty(dataType = "string", value = "The bot's intent", required = true)
	String intent;

	@ApiModelProperty(value = "The bot's response", required = true)
	String response;

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	
}
