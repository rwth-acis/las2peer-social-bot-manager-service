package i5.las2peer.services.socialBotManagerService.parser.creation.function;

import java.net.URL;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import i5.las2peer.services.socialBotManagerService.parser.creation.parameter.CreationParameter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(parent = ServiceType.class, value = "OpenAPI")
public class OpenAPI extends ServiceType {

	@ApiModelProperty(value = "Which intent should activate the service access?", required = true, example = "greet")
	String intent;
	
	@ApiModelProperty(value = "The url of the service", required = true, example = "https://petstore3.swagger.io/")
	URL baseURL;
	
	@ApiModelProperty(value = "The name of the service function", required = true, example = "addPet")
	String functionName;
	
	@ApiModelProperty(value = "URL of the openAPI definition", required = true, example = "https://petstore.swagger.io/v2/swagger.json")
	URL swaggerURL;

	@ApiModelProperty(value = "URL of the OpenAPI definition", required = false)
	Collection<CreationParameter> parameters;

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

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}
	
	public Collection<CreationParameter> getParameters() {
		return parameters;
	}
	
	public void setParameters(Collection<CreationParameter> parameters) {
		this.parameters = parameters;
	}

	@JsonIgnore
	public ServiceAccessType getAccessType() {
		return ServiceAccessType.OPENAPI;
	}

}
