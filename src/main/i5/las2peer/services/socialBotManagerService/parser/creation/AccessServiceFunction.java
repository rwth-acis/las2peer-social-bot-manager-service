package i5.las2peer.services.socialBotManagerService.parser.creation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = Function.class, value = "AccessService")
public class AccessServiceFunction extends Function {

    public ServiceAccessType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceAccessType serviceType) {
		this.serviceType = serviceType;
	}

	@ApiModelProperty(value = "Type of Service Access")
    ServiceAccessType serviceType;

	
}
