package i5.las2peer.services.socialBotManagerService.parser.creation;

import io.swagger.annotations.ApiModelProperty;

public class CreationParameter {
	
	@ApiModelProperty(value = "What is the Parameter Name?", required = true, example = "petName")
	String parameterName;
	@ApiModelProperty(value = "How should the bot get the parameter value", required = true)
	CreationParameterType askType;
	@ApiModelProperty(value = "Which content should it have?", required = true)
	String content;
	
	public String getParameterName() {
		return parameterName;
	}
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	public CreationParameterType getAskType() {
		return askType;
	}
	public CreationParameter(String parameterName, CreationParameterType askType, String content) {
		super();
		this.parameterName = parameterName;
		this.askType = askType;
		this.content = content;
	}
	public void setAskType(CreationParameterType askType) {
		this.askType = askType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

}
