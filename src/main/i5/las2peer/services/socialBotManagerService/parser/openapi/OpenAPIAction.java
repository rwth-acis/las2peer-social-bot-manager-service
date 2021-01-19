package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.model.GeneratorFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import net.minidev.json.JSONObject;

/**
 * Associates the parameter values for a ServiceFunction.
 *
 */
public class OpenAPIAction {

	private FunctionInterface function;

	private JSONObject bodyParameter;

	private Map<String, String> queryParameters;

	private Map<String, String> pathParameters;

	private ResponseParseMode responseParseMode;

	/**
	 * Create OpenAPI Action with only static parameters
	 * 
	 * @param sf service function need to have a valid base url
	 */
	public OpenAPIAction(ServiceFunction sf) {
		this((FunctionInterface) sf);
	}
	
	public OpenAPIAction(FunctionInterface sf) {
		
		assert sf != null;
		assert sf.getBasePath() != null : "function " + sf.getFunctionName() + " has null base url";
		assert !sf.getBasePath().contentEquals(""): "function " + sf.getFunctionName() + " has empty base url";
		
		this.function = sf;
		this.pathParameters = new HashMap<>();
		this.queryParameters = new HashMap<>();
		this.bodyParameter = null;
		this.initParameters();
	}

	/**
	 * Create OpenAPI Action with parameters
	 * @param sf
	 * @param parameters
	 */
	public OpenAPIAction(FunctionInterface sf, Map<String, String> parameters) {
		this(sf);
		if (parameters != null)
			this.addParameters(parameters);
	}

	/**
	 * Add the parameters with static content values
	 */
	private void initParameters() {		
		assert this.function != null;
		
		for(ServiceFunctionAttribute attr :this.function.getAllAttributes()) {			
			if(attr.hasContent())
				this.addParameter(attr, attr.getContent());				
		}		
	}
	
	/**	
	 * Send HTTP Request 
	 * Need all required parameters to be filled
	 * 
	 * @return open api response
	 */
	public OpenAPIResponse execute() throws IllegalStateException {
		
		if(!this.validate())
			throw new IllegalStateException("Not all required parameters are filled");
		
		ClientResponse response = OpenAPIConnector.sendRequest(this);
		return new OpenAPIResponse(response);
	}
	
	public DialogueAct generate(String intent, String template) {
		if(this.function instanceof GeneratorFunction) {
			System.out.println("--- Generator Function");
			Map<String, String> parameters = this.pathParameters;
			parameters.putAll(this.queryParameters);
			
			return ((GeneratorFunction) this.function).generate(intent, template, parameters);
		}
		
		return null;
		
	}
	

	/**
	 * Returns the base path of the service this action corresponds to.
	 * 
	 * @return
	 */
	public String getBasePath() {
		invariant();

		if(this.function.getBasePath() == null || this.function.getBasePath().length() < 1)
			return "";
		
		String baseURL = this.function.getBasePath();
		while (baseURL.substring(baseURL.length() - 1).contentEquals("/"))
			baseURL = baseURL.substring(0, baseURL.length() - 1);

		return baseURL;
	}

	/**
	 * Returns the function path with filled parameter values
	 * Needs the path parameters to be filled
	 * 
	 * @return
	 */
	public String getFunctionPath() {
		invariant();
		
		if(!this.validate())
			throw new IllegalStateException("Not all required path parameters are filled yet " + this.getFunctionName());
				
		String methodURL = this.function.getFunctionPath();
		while (methodURL.length() > 0 && methodURL.charAt(0) == '/')
			methodURL = methodURL.substring(1);
	

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
		assert value != null : "add parameter " + attr.getName() + " no value";
		System.out.println("add parameter " + attr.getName() + " " + attr.getParameterType() + ", value: " + value);
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
		if(name.contentEquals("botEventId"))
			return true;
		
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
	
	public FunctionInterface getFunction() {
		return this.function;
	}
	
	public String getFunctionName() {
		return this.function.getFunctionName();
	}
	
	public String getRequestMethod() {
		invariant();
		return function.getHttpMethod().toUpperCase();
	}
	
	public ResponseParseMode getResponseParseMode() {
		if(responseParseMode != null)
		return responseParseMode;
		return ResponseParseMode.MESSAGE_TEXT;
	}

	public void setResponseParseMode(ResponseParseMode responseParseMode) {
		this.responseParseMode = responseParseMode;
	}
	
	public boolean validate() {
		Collection<ServiceFunctionAttribute> attrs = this.function.getRequiredAttributes();
		boolean flag = true;
		for(ServiceFunctionAttribute attr :attrs) {		
			if(attr.getParameterType() == ParameterType.PATH && !this.pathParameters.containsKey(attr.getName())) {
				System.out.println("missing parameter: " + attr.getName());
				flag = false;				
			}
		}
		
		return flag;
	}
	
	public void invariant() {
		assert this.function != null : "function of OpenAPIAction is null";
		assert this.function.getHttpMethod() != null : "method of function is null: " + function.getFunctionName();
		assert RequestMethod
				.validate(this.function.getHttpMethod().toUpperCase()) : "HTTP method of OpenAPIAction is invalid";
		
	}
		
	@Override
	public String toString() {
		return "OpenAPIAction [bodyParameter=" + bodyParameter + ", queryParameters=" + queryParameters
				+ ", pathParameters=" + pathParameters + ", getRequestMethod()=" + getRequestMethod()
				+ ", getBasePath()=" + getBasePath() + ", getFunctionPath()=" + getFunctionPath() + "]";
	}

}
