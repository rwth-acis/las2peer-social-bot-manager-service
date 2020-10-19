package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;

public class OpenAPIConnector {

    public static ServiceFunction readFunction(ServiceFunction action) {

	assert action != null : "read open api function: action parameter is null";
	assert action.getServiceName() != null : "read open api function: action has no service url specified";
	assert (action.getFunctionName() != null || (action.getFunctionPath() != null
		&& action.getHttpMethod() != null)) : "read open api function: no function specified";

	// base url
	String baseUrl = action.getServiceName();
	String last = baseUrl.substring(baseUrl.length() - 1);
	if (last.contentEquals("/"))
	    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

	// retrieve path of model definition
	String modelUrl = getSwaggerDocument(baseUrl);
	if (modelUrl == null) {
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
	    action.setServiceName(baseUrl);
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
	action.setServiceName(baseUrl);
	return action;
    }

    public static String sendRequest(OpenAPIAction action) {

	assert action != null : "action parameter is null";
	assert action.getFunction() != null : "action parameter service function is null";

	ServiceFunction sf = action.getFunction();
	assert sf.getFunctionPath() != null : "no function path";
	assert sf.getHttpMethod() != null : "no http method";
	assert sf.getServiceName() != null : "no service name";

	System.out.println("perform REST action: " + action.toString());

	MiniClient client = new MiniClient();
	client.setConnectorEndpoint(action.getBasePath());
	if (sf.getProduces() == null)
	    sf.setProduces("text/plain");

	System.out.println("client: " + client);

	HashMap<String, String> headers = new HashMap<String, String>();
	ClientResponse response = client.sendRequest(action.getRequestMethod(), action.getFunctionPath(),
		action.getBodyParameter().toJSONString(), sf.getConsumes(), sf.getProduces(), headers);

	System.out.println("Response: " + response.getHttpCode() + response.getResponse() + response.getRawResponse());
	return response.getResponse();

    }

    private static String getSwaggerDocument(String baseUrl) {

	if (baseUrl.contains(".json"))
	    return baseUrl;

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

	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (IOException e) {

	}

	return null;

    }

}
