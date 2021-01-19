package i5.las2peer.services.socialBotManagerService.parser.creation.parameter;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = CreationParameter.class, value = "Static Content")
public class ContentCreationParameter extends CreationParameter {

	@ApiModelProperty(value = "Which content should it have?", required = true)
	String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
