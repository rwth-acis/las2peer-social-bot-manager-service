package i5.las2peer.services.socialBotManagerService.parser.creation.function;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = CreatorFunction.class, value = "Service Access")
public class ServiceAccessFunction extends CreatorFunction {

	@ApiModelProperty(required = true, value = "Type of Service Access")
    AccessService serviceType;
	
	public ServiceAccessFunction() {
		
	}
	
	public AccessService getServiceType() {
		return serviceType;
	}

	public void setServiceType(AccessService serviceType) {
		this.serviceType = serviceType;
	}

	
}
