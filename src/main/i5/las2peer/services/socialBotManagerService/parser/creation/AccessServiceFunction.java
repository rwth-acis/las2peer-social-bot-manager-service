package i5.las2peer.services.socialBotManagerService.parser.creation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = Function.class, value = "AccessService")
public class AccessServiceFunction extends Function {

	@ApiModelProperty(value = "Type of Service Access")
    ServiceType serviceType;
	
    public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}



	
}
