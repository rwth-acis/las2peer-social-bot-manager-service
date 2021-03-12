package i5.las2peer.services.socialBotManagerService.parser.creation.function;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import i5.las2peer.services.socialBotManagerService.parser.creation.parameter.CreationParameter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = AccessService.class, value = "las2peer")
public class Las2peerAccessService extends AccessService {

	@ApiModelProperty(value = "Which intent should activate the service access?", required = true, example = "greet")
	String nluIntent;
	
	@ApiModelProperty(value = "the service alias", required = true, example = "SBFManager")
	String serviceAlias;
	
	@ApiModelProperty(value = "The function Name", required = true, example = "getBots")
	String operationName;

	@ApiModelProperty(value = "Response message after successfull service access", required = true, example = "I did the Service access!")
	String responseMessage;
	
	@ApiModelProperty(required = false)
	String domain;
	
	@ApiModelProperty(required = false)
	String errorResponse;
	
	@ApiModelProperty(value = "URL of the OpenAPI definition", required = false)
	Collection<CreationParameter> parameters;

	public String getNluIntent() {
		return nluIntent;
	}

	public void setNluIntent(String intent) {
		this.nluIntent = intent;
	}

	public Collection<CreationParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Collection<CreationParameter> parameters) {
		this.parameters = parameters;
	}

	public String getServiceAlias() {
		return serviceAlias;
	}

	public void setServiceAlias(String serviceAlias) {
		this.serviceAlias = serviceAlias;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String functionName) {
		this.operationName = functionName;
	}
	
	
	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}


	@JsonIgnore
	public String getAccessType() {
		return "las2peer";
	}

}
