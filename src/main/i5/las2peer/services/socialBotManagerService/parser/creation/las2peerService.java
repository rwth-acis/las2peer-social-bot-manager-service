package i5.las2peer.services.socialBotManagerService.parser.creation;

import io.swagger.annotations.ApiModel;

@ApiModel(parent = ServiceType.class, value = "las2peer")
public class las2peerService extends ServiceType {
	
	String serviceAlias;
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
