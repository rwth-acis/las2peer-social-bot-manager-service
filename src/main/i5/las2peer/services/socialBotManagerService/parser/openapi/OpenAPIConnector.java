package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.Service;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.ServiceType;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;

/**
 * Handles the interaction with OpenAPI specifications
 *
 */
public class OpenAPIConnector {

	/**
	 * Reads the Swagger definition of a service function
	 * 
	 * @param action that defines the service function location
	 * @return new service action including the swagger information
	 */
	public static ServiceFunction readFunction(ServiceFunction action) {

		assert action != null : "read open api function: action parameter is null";
		assert action.getServiceName() != null
				|| action.getSwaggerUrl() != null : "read open api function: action has no service url specified";
		assert (action.getFunctionName() != null || (action.getFunctionPath() != null
				&& action.getHttpMethod() != null)) : "read open api function: no function specified";

		System.out.println("read function " + action.getFunctionName() + " base path: " + action.getBasePath()
				+ " serviceName: " + action.getServiceName());

		Service service = action.getService();

		// base url
		String baseUrl = action.getBasePath();
		String last = baseUrl.substring(baseUrl.length() - 1);
		if (last.contentEquals("/"))
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

		// retrieve path of model definition
		String modelUrl = null;
		if (action.getSwaggerUrl() != null && action.getSwaggerUrl().startsWith("http"))
			modelUrl = action.getSwaggerUrl();

		if (modelUrl == null)
			modelUrl = getSwaggerLocation(baseUrl);

		if (modelUrl == null || modelUrl.contentEquals("")) {
			System.out.println("swagger definition not found");
			return null;
		}

		// read model definition
		OpenApi3 model = OpenAPIReaderV3.readModel(modelUrl);
		if (model == null) {
			System.out.println("open api model not found: " + modelUrl);
			return null;
		}

		// read function V2
		if (!model.isValid()) {
			SwaggerDeserializationResult swaggerParseResult = new SwaggerParser().readWithInfo(modelUrl, null, true);
			Swagger swagger = swaggerParseResult.getSwagger();
			if (action.getFunctionPath() != null && action.getHttpMethod() != null) {
				action = OpenAPIReaderV2.readAction(swagger, action.getFunctionPath(), action.getHttpMethod());
			} else if (action.getFunctionName() != null) {
				action = OpenAPIReaderV2.readAction(swagger, action.getFunctionName());
			} else {
				System.out.println("service function not defined");
			}
			action.setService(service);
			action.setServiceName(baseUrl);
			System.out.println(action);
			return action;
		}

		// read function V3
		if (action.getFunctionPath() != null && action.getHttpMethod() != null) {
			action = OpenAPIReaderV3.readAction(model, action.getFunctionPath(), action.getHttpMethod());
		} else if (action.getFunctionName() != null) {
			action = OpenAPIReaderV3.readAction(model, action.getFunctionName());
		} else {
			System.out.println("service function not defined");
		}

		assert action.getFunctionDescription() != null : "service has no description";

		action.setService(service);
		action.setServiceName(baseUrl);
		return action;
	}

	public static ClientResponse sendRequest(ServiceFunction sf) {

		assert sf != null : "service function is null";
		assert sf.getBasePath() != null : "no base path";
		assert sf.getFunctionPath() != null : "no function path";
		assert sf.getHttpMethod() != null : "no http method";
		assert sf.getServiceName() != null : "no service name";

		OpenAPIAction action = new OpenAPIAction(sf);
		return sendRequest(action);
	}

	public static ClientResponse sendRequest(ServiceFunction sf, String loginName, String password) {

		assert sf != null : "service function is null";
		assert sf.getBasePath() != null : "no base path";
		assert sf.getFunctionPath() != null : "no function path";
		assert sf.getHttpMethod() != null : "no http method";
		assert sf.getServiceName() != null : "no service name";
		assert sf.getServiceType() == ServiceType.SERVICE;
		assert loginName != null;
		assert password != null;

		OpenAPIAction action = new OpenAPIAction(sf);
		return sendRequest(action, loginName, password);
	}

	public static ClientResponse sendRequest(OpenAPIAction action, String loginName, String password) {

		assert action != null;
		assert action.getFunction() != null;

		MiniClient client = new MiniClient();
		client.setConnectorEndpoint(action.getBasePath());
		client.setLogin(loginName, password);

		return sendRequest(client, action);
	}

	/**
	 * Send a Request to an Service Function that has a OpenAPI documentation.
	 * 
	 * @param action The service access action
	 * @return The response of the accessed service
	 */
	public static ClientResponse sendRequest(OpenAPIAction action) {

		assert action != null : "action parameter is null";
		assert action.getFunction() != null : "action parameter service function is null";

		System.out.println("perform REST action: " + action.toString());

		MiniClient client = new MiniClient();
		client.setConnectorEndpoint(action.getBasePath());

		ServiceFunction sf = action.getFunction().asServiceFunction();
		System.out.println("Action " + sf.getActionType() + " " + sf.getServiceType());
		ClientResponse response = null;
		if (sf.getActionType() != ActionType.FUNCTION && sf.getServiceType() == ServiceType.SERVICE) {

			String loginName = "alice";
			String password = "pwalice";
			client.setLogin(loginName, password);
			response = sendRequest(client, action);

		}
		if (response == null) {
			System.out.println("------ resend action");
			response = sendRequest(client, action);
			assert response != null;
		}

		System.out.println("received response " + response);
		return response;
	}

	private static ClientResponse sendRequest(MiniClient client, OpenAPIAction action) {

		assert client != null : "client is null";
		assert action != null : "action is null";
		assert action.getFunction() != null : "open api action has no service function";

		ServiceFunction sf = action.getFunction().asServiceFunction();

		assert sf.getHttpMethod() != null : "no http method";

		String bodyContent = "";
		if (action.getBodyParameter() != null) {
			bodyContent = action.getBodyParameter().toJSONString();
		}
		String consumes = sf.getConsumes();
		if (consumes == null || consumes.contentEquals("")) {
			if (sf.getHttpMethod().equalsIgnoreCase("GET"))
				consumes = "text/plain";
			if (sf.getHttpMethod().equalsIgnoreCase("POST"))
				consumes = "application/json";
		}

		String produces = sf.getProduces();
		if (produces == null || produces.contentEquals("")) {
			if (sf.getHttpMethod().equalsIgnoreCase("GET"))
				produces = "application/json";
			if (sf.getHttpMethod().equalsIgnoreCase("POST"))
				produces = "text/plain";
		}

		System.out.println("base url " + action.getBasePath() + " function path " + action.getFunctionPath());
		ClientResponse response = null;
		try {

			HashMap<String, String> headers = new HashMap<String, String>();
			response = client.sendRequest(action.getRequestMethod(), action.getFunctionPath(), bodyContent, consumes,
					produces, headers);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("Response: " + response.getHttpCode() + " " + response.getResponse());

		return response;

	}

	/**
	 * Retrieve the JSON swagger documentation URL of a web service.
	 * 
	 * @param Base URL of the web service
	 * @return Swagger document location URL as String
	 */
	public static String getSwaggerLocation(String baseUrl) {

		if (baseUrl.contains(".json"))
			return baseUrl;

		if (baseUrl.length() > 1 && baseUrl.endsWith("/"))
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

		URL url;
		try {
			String res = baseUrl + "/swagger.json";
			url = new URL(res);

			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("HEAD");
			int responseCode = huc.getResponseCode();

			if (responseCode == 200)
				return res;

			res = baseUrl + "/v2/swagger.json";
			url = new URL(res);
			huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("HEAD");
			responseCode = huc.getResponseCode();

			if (responseCode == 200)
				return res;

			res = baseUrl + "/api/v3/openapi.json";
			url = new URL(res);
			huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("HEAD");
			responseCode = huc.getResponseCode();

			if (responseCode == 200)
				return res;

			res = baseUrl + "/api/v2/openapi.json";
			url = new URL(res);
			huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("HEAD");
			responseCode = huc.getResponseCode();

			if (responseCode == 200)
				return res;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {

		}

		return null;

	}

	public static Collection<String> readEnums(ServiceFunction sf, String key) {

		assert sf != null : "service function is null";
		assert sf.getServiceName() != null : "service has no name";

		System.out.println("read enums: " + sf.getServiceName() + " " + sf.getFunctionPath() + " with key: " + key);

		if (sf.getHttpMethod() == null)
			sf.setHttpMethod("GET");

		assert sf.getHttpMethod().equalsIgnoreCase("GET") : "function is not a GET request";

		if (sf.getProduces() == null)
			sf.setProduces("application/json");

		sf.setConsumes("text/plain");

		OpenAPIAction request = new OpenAPIAction(sf);

		return readEnums(request, key);
	}

	public static Collection<String> readEnums(OpenAPIAction request, String key) {

		ClientResponse response = sendRequest(request);
		System.out.println("enum response " + response);
		if (response == null)
			return null;

		if (response.getHttpCode() >= 300)
			return null;

		JsonElement jsonElement = JsonParser.parseString(response.getResponse());

		if (key != null && !key.contentEquals("")) {
			Collection<String> res = searchValuesByKey(jsonElement, key);
			return res;
		}

		if (!jsonElement.isJsonArray()) {
			System.out.println("response is not an json array");
			return null;
		}

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		return readEnums(jsonArray);
	}

	public static Collection<String> searchValuesByKey(JsonElement json, String key) {

		if (json.isJsonNull())
			return null;

		if (json.isJsonPrimitive())
			return null;

		Collection<String> res = new LinkedList<String>();

		if (json.isJsonArray()) {
			JsonArray jsonArray = (JsonArray) json;
			for (JsonElement element : jsonArray) {
				Collection<String> values = searchValuesByKey(element, key);
				if (res != null && values != null)
					res.addAll(values);
			}
		}

		if (json.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) json;
			if (jsonObject.has(key)) {
				JsonElement element = jsonObject.get(key);
				if (element.isJsonPrimitive())
					res.add(element.getAsString());

			} else {
				for (Entry<String, JsonElement> ele : jsonObject.entrySet()) {
					Collection<String> values = searchValuesByKey(ele.getValue(), key);
					if (values != null)
						res.addAll(values);
				}
			}
		}

		return res;
	}

	public static Collection<String> readEnums(JsonArray jsonArray) {

		assert jsonArray != null : "jsonArray is null";
		assert jsonArray.isJsonArray() : "jsonArray is no jsonArray";

		Collection<String> res = new LinkedList<>();
		for (JsonElement ele : jsonArray) {

			if (ele.isJsonPrimitive()) {
				String pri = ele.getAsString();
				res.add(pri);

			} else if (ele.isJsonObject()) {
				JsonObject obj = ele.getAsJsonObject();

				if (obj.keySet().contains("name")) {
					JsonElement jel = obj.get("name");
					if (jel.isJsonPrimitive()) {
						String pri = jel.getAsString();
						res.add(pri);
					}
				}

				else if (obj.keySet().contains("id")) {
					JsonElement jel = obj.get("id");
					if (jel.isJsonPrimitive()) {
						String pri = jel.getAsString();
						res.add(pri);
					}
				}
			}
		}

		return res;

	}

	public static Collection<ServiceFunction> getFunctions(String swaggerURL) {
		assert swaggerURL != null : "swagger URL is null";
		assert !swaggerURL.contentEquals("") : "swagger url is empty";

		try {
			URL url = new URL(swaggerURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(swaggerURL + " is not valid URL");
		}

		Collection<String> operationIds = getFunctionNames(swaggerURL);
		for (String functionName : operationIds) {

		}

		return null;

	}

	public static Collection<String> getFunctionNames(String swaggerURL) {

		assert swaggerURL != null;
		System.out.println("find functions for " + swaggerURL);
		String modelUrl = swaggerURL;

		// read function V2
		SwaggerDeserializationResult swaggerParseResult = new SwaggerParser().readWithInfo(modelUrl, null, true);
		Swagger swagger = swaggerParseResult.getSwagger();
		assert swagger != null;
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";
		try {
			jsonString = mapper.writeValueAsString(swagger);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		JsonElement jsonElement = JsonParser.parseString(jsonString);

		return searchValuesByKey(jsonElement, "operationId");

	}

	public static Collection<ServiceFunctionAttribute> getParameters(String serviceURL, String functionName) {

		assert serviceURL != null;
		assert functionName != null;

		String swaggerURL = getSwaggerLocation(serviceURL);

		URL swagger = null;
		URL su = null;
		try {
			swagger = new URL(swaggerURL);
			su = new URL(serviceURL);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Service service = new Service(null, functionName, su, swagger);
		ServiceFunction function = new ServiceFunction("", service, functionName);
		ServiceFunction parsedFunction = readFunction(function);

		return parsedFunction.getAllAttributes();
	}

	public static String getServiceName(String serviceAlias) {

		String name = "";

		switch (serviceAlias) {

		case "i5.las2peer.services.weatherService.WeatherService":
			name = "weather";
			break;

		case "i5.las2peer.services.weatherService.CoronaService":
			name = "corona";
			break;

		case "i5.las2peer.services.socialBotManagerService.SocialBotManagerService":
			name = "sbfmanager";
			break;

		}
		
		return name;

	}

	public static Collection<String> getOperationNames(String serviceAlias, String address) {

		String name = getServiceName(serviceAlias);
		if (name.contentEquals(""))
			return null;

		if(address.endsWith("/"))
			address = address.substring(0, address.length() - 1);
		String swaggerURL = address + "/" + name + "/swagger.json";
		System.out.println("Search for " + swaggerURL);

		return getFunctionNames(swaggerURL);
	}

}
