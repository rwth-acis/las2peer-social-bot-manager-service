package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import net.minidev.json.JSONObject;

public class OpenAPIAction {

	private ServiceFunction function;

	private JSONObject bodyParameter;

	private Map<String, String> queryParameters;

	private Map<String, String> pathParameters;

	private ResponseParseMode responseParseMode;

	public OpenAPIAction(ServiceFunction sf) {
		assert sf != null;
		this.function = sf;
		this.initParameters();
	}

	public OpenAPIAction(ServiceFunction sf, Map<String, String> parameters) {
		this(sf);
		if (parameters != null)
			this.addParameters(parameters);
	}

	private void initParameters() {		
		assert this.function != null;
		
		for(ServiceFunctionAttribute attr :this.function.getAllAttributes()) {
			
			if(attr.hasContent())
				this.addParameter(attr, attr.getContent());
				
		}
		
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

	public void addParameters(Map<String, String> parameters) {

		for (Entry<String, String> entry : parameters.entrySet()) {
			ServiceFunctionAttribute attr = this.function.getAttribute(entry.getKey());
			if (attr == null)
				throw new IllegalArgumentException(
						"parameter " + entry.getKey() + " not contained in " + function.getFunctionName());
			this.addParameter(attr, entry.getValue());
		}

	}

	public void addParameter(ServiceFunctionAttribute attr, String value) {
		assert attr != null;
		assert value != null;
		assert this.function.contains(attr);
		assert attr.getParameterType() == ParameterType.PATH || attr.getParameterType() == ParameterType.QUERY;

		if (attr.getParameterType() == ParameterType.PATH)
			addPathParameter(attr.getName(), value);

		if (attr.getParameterType() == ParameterType.QUERY)
			addQueryParameter(attr.getName(), value);

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
		assert name != null : "no name";
		assert value != null : "no value";
		assert validatePathParameter(name, value) : "path parameter invalid";

		if (this.pathParameters == null)
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

	public boolean validate() {
		Collection<ServiceFunctionAttribute> attrs = this.function.getRequiredAttributes();
		for(ServiceFunctionAttribute attr :attrs) {
			if(attr.getParameterType() == ParameterType.PATH && !this.pathParameters.containsKey(attr.getName()))
				return false;
		}
		
		return true;
	}
	
	public void invariant() {
		assert this.function != null : "function of OpenAPIAction is null";
		assert this.function.getHttpMethod() != null : "method of function is null";
		assert RequestMethod
				.validate(this.function.getHttpMethod().toUpperCase()) : "HTTP method of OpenAPIAction is invalid";
		assert this.function.getServiceName() != null : "function of OpenAPIAction has no base url";
	}

	@Override
	public String toString() {
		return "OpenAPIAction [bodyParameter=" + bodyParameter + ", queryParameters=" + queryParameters
				+ ", pathParameters=" + pathParameters + ", getRequestMethod()=" + getRequestMethod()
				+ ", getBasePath()=" + getBasePath() + ", getFunctionPath()=" + getFunctionPath() + "]";
	}

	public ResponseParseMode getResponseParseMode() {
		if(responseParseMode != null)
		return responseParseMode;
		return ResponseParseMode.MESSAGE_TEXT;
	}

	public void setResponseParseMode(ResponseParseMode responseParseMode) {
		this.responseParseMode = responseParseMode;
	}

	public String execute() {
		if(!this.validate())
			throw new IllegalStateException("Not all required parameters are filled");
		
		ClientResponse response = OpenAPIConnector.sendRequest(this);
		return response.toString();
	}

}
