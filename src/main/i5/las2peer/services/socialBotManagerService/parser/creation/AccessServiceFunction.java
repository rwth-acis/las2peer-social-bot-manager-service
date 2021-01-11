package i5.las2peer.services.socialBotManagerService.parser.creation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(parent = Function.class, value = "AccessService")
public class AccessServiceFunction extends Function {

	@ApiModelProperty(value = "Type of Service Access")
    ServiceType serviceType;
	
	public AccessServiceFunction() {
		
	}
	
	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}



	
}
