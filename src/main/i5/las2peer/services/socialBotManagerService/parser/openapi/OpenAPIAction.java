package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.util.List;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import net.minidev.json.JSONObject;

public class OpenAPIAction {

    private ServiceFunction function;

    private JSONObject bodyParameter;

    private Map<String, String> queryParameters;

    private Map<String, String> pathParameters;

    public String getRequestMethod() {
	invariant();
	return function.getHttpMethod().toUpperCase();
    }

    public String getBasePath() {
	invariant();

	String baseURL = this.function.getServiceName();
	while (baseURL.substring(baseURL.length() - 1).contentEquals("/")) {
	    baseURL = baseURL.substring(0, baseURL.length() - 1);
	}

	return baseURL;
    }

    public String getFunctionPath() {
	invariant();

	String methodURL = this.function.getFunctionPath();
	while (methodURL.charAt(0) == '/') {
	    methodURL = methodURL.substring(1);
	}

	return methodURL;
    }

    public ServiceFunction getFunction() {
	return function;
    }

    public void setFunction(ServiceFunction function) {
	this.function = function;
    }

    public JSONObject getBodyParameter() {
	return bodyParameter;
    }

    public void setBodyParameter(JSONObject bodyParameter) {
	this.bodyParameter = bodyParameter;
    }

    public void setBodyParameter(Object json) {
	this.bodyParameter = (JSONObject) json;
    }

    public Map<String, String> getQueryParameters() {
	return queryParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
	this.queryParameters = queryParameters;
    }

    public void addQueryParameter(String name, String value) {
	assert validateQueryParameter(name, value) : "query parameter invalid";

	this.queryParameters.put(name, value);
    }

    public boolean validateQueryParameter(String name, String value) {
	List<ServiceFunctionAttribute> attrs = function.getQueryAttributes();
	for(ServiceFunctionAttribute attr: attrs) {
	    if (attr.getName().contentEquals(name))
		    return true;
	}
	return false;
    }

    public Map<String, String> getPathParameters() {
	return pathParameters;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
	this.pathParameters = pathParameters;
    }

    public void addPathParameter(String name, String value) {
	assert validatePathParameter(name, value) : "path parameter invalid";

	this.pathParameters.put(name, value);
    }

    public boolean validatePathParameter(String name, String value) {
	List<ServiceFunctionAttribute> attrs = function.getPathAttributes();
	for (ServiceFunctionAttribute attr : attrs) {
	    if (attr.getName().contentEquals(name))
		return true;
	}
	return false;
    }

    public void invariant() {
	assert this.function != null : "function of OpenAPIAction is null";
	assert this.function.getHttpMethod() != null : "method of function is null";
	assert RequestMethod
		.validate(this.function.getHttpMethod().toUpperCase()) : "HTTP method of OpenAPIAction is invalid";
	assert this.function.getServiceName() != null : "function of OpenAPIAction has no base url";
	assert this.function.getFunctionName() != null : "function of OpenAPIAction has no function url";
    }

    @Override
    public String toString() {
	return "OpenAPIAction [bodyParameter=" + bodyParameter + ", queryParameters=" + queryParameters
		+ ", pathParameters=" + pathParameters + ", getRequestMethod()=" + getRequestMethod()
		+ ", getBasePath()=" + getBasePath() + ", getFunctionPath()=" + getFunctionPath() + "]";
    }

}