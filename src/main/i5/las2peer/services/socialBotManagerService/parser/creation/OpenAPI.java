package i5.las2peer.services.socialBotManagerService.parser.creation;

import java.net.URL;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = ServiceType.class, value = "OpenAPI")
public class OpenAPI extends ServiceType {
	
	@ApiModelProperty(value = "The url of the service", required = true, example = "https://petstore3.swagger.io/")
	URL baseURL;
	@ApiModelProperty(value = "The name of the service function", required = true, example = "addPet")
	String functionName;
	@ApiModelProperty(value = "URL of the openAPI definition", required = true, example = "https://petstore.swagger.io/v2/swagger.json")
	URL swaggerURL;
	
	public URL getSwaggerURL() {
		return swaggerURL;
	}
	public void setSwaggerURL(URL swaggerURL) {
		this.swaggerURL = swaggerURL;
	}
	public URL getBaseURL() {
		return baseURL;
	}
	public void setBaseURL(URL baseURL) {
		this.baseURL = baseURL;
	}
	public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
}
