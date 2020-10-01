package i5.las2peer.services.socialBotManagerService.parser.creation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = Function.class, value = "AccessService")
public class AccessServiceFunction extends Function {

	@ApiModelProperty(dataType = "string", value = "The url of the service you want to access", required = true, example = "https://petstore3.swagger.io/")
	private String serviceURL;

	@ApiModelProperty(dataType = "string", value = "The function that you want to access", required = true, example = "addPet")
	private String operationID;

	public String getServiceURL() {
		return serviceURL;
	}

	public void setServiceURL(String serviceURL) {
		this.serviceURL = serviceURL;
	}

	public String getOperationID() {
		return operationID;
	}

	public void setOperationID(String operationID) {
		this.operationID = operationID;
	}

}
