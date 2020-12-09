package i5.las2peer.services.socialBotManagerService.parser.creation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = ServiceType.class, value = "las2peer")
public class Las2peer extends ServiceType {
	
	@ApiModelProperty(value = "the service alias", required = true, example = "SBFManager")
	String serviceAlias;
	@ApiModelProperty(value = "The function Name", required = true, example = "getBots")
	String functionName;
	
	public String getServiceAlias() {
		return serviceAlias;
	}
	public void setServiceAlias(String serviceAlias) {
		this.serviceAlias = serviceAlias;
	}
	public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
}
