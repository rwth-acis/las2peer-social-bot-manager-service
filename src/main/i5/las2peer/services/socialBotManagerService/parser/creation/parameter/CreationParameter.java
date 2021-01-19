package i5.las2peer.services.socialBotManagerService.parser.creation.parameter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "askType")
@JsonSubTypes({ @JsonSubTypes.Type(value = AskForItCreationParameter.class, name = "Ask for it!"),
		@JsonSubTypes.Type(value = ContentCreationParameter.class, name = "Static Content") })
@ApiModel(discriminator = "askType", subTypes = { AskForItCreationParameter.class, ContentCreationParameter.class })
public class CreationParameter {

	@ApiModelProperty(value = "What is the Parameter Name?", required = true, example = "petName")
	String parameterName;

	@ApiModelProperty(dataType = "string", allowableValues = "Static Content, Ask for it!", required = true, value = "How should the bot get the parameter value")
	CreationParameterType askType;

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public CreationParameterType getAskType() {
		return askType;
	}

	public void setAskType(CreationParameterType askType) {
		this.askType = askType;
	}

}
