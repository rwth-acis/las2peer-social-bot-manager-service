package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import net.minidev.json.JSONObject;

public class OpenAPIAction {

	private ServiceFunction function;

	private JSONObject bodyParameter;

	private Map<String, String> queryParameters;

	private Map<String, String> pathParameters;

	private ResponseParseMode responseParseMode;

	public OpenAPIAction() {
		this.responseParseMode = ResponseParseMode.TEXT;
	}

	public OpenAPIAction(ServiceFunction sf) {
		this();
		this.function = sf;
	}

	public String getRequestMethod() {
		invariant();
		return function.getHttpMethod().toUpperCase();
	}

	public String getBasePath() {
		invariant();

		String baseURL = this.function.getBasePath();
		while (baseURL.substring(baseURL.length() - 1).contentEquals("/")) {
			baseURL = baseURL.substring(0, baseURL.length() - 1);
		}

		return baseURL;
	}

	public String getFunctionPath() {
		invariant();

		String methodURL = this.function.getFunctionPath();
		while (methodURL.length() > 0 && methodURL.charAt(0) == '/') {
			methodURL = methodURL.substring(1);
		}
		
		if (getPathParameters() != null) {
			for (Entry<String, String> para : getPathParameters().entrySet())
				methodURL = methodURL.replace("{" + para.getKey() + "}", para.getValue());
		}

		if (getQueryParameters() != null) {
			
			Iterator<Entry<String, String>> entries = getQueryParameters().entrySet().iterator();
			if (entries.hasNext()) {
				Entry<String, String> entry = entries.next();
				methodURL = methodURL.concat("?").concat(entry.getKey()).concat("=").concat(entry.getValue());
			}
			
			for (; entries.hasNext();) {
				Entry<String, String> entry = entries.next();
				methodURL = methodURL.concat("&").concat(entry.getKey()).concat("=").concat(entry.getValue());
			}
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
		for (ServiceFunctionAttribute attr : attrs) {
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
		assert name != null: "no name";
		assert value != null: "no value";
		assert validatePathParameter(name, value) : "path parameter invalid";
		
		if(this.pathParameters == null)
			this.pathParameters = new HashMap<>();
		
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

	public ResponseParseMode getResponseParseMode() {
		return responseParseMode;
	}

	public void setResponseParseMode(ResponseParseMode responseParseMode) {
		this.responseParseMode = responseParseMode;
	}

}
