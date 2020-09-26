package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import net.minidev.json.JSONObject;

public class OpenAPIConnector {

    public static ServiceFunction readFunction(ServiceFunction action) {

	assert action != null : "read open api function: action parameter is null";
	assert action.getServiceName() != null : "read open api function: action has no service url specified";
	assert (action.getFunctionName() != null || (action.getFunctionPath() != null
		&& action.getHttpMethod() != null)) : "read open api function: no function specified";

	// retrieve path of model definition
	String modelUrl = getSwaggerDocument(action.getServiceName());
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
		return action;
	    } else if (action.getFunctionName() != null) {
		action = OpenAPIReaderV2.readAction(swagger, action.getFunctionName());
		return action;
	    } else {
		System.out.println("service function not defined");
	    }

	}

	// read function V3
	if (action.getFunctionPath() != null && action.getHttpMethod() != null) {
	    action = OpenAPIReaderV3.readAction(model, action.getFunctionPath(), action.getHttpMethod());
	} else if (action.getFunctionName() != null) {
	    action = OpenAPIReaderV3.readAction(model, action.getFunctionName());
	} else {
	    System.out.println("service function not defined");
	}

	return action;
    }

    public String sendRequest(ServiceFunction action, JSONObject body) {

	if (!action.getActionType().equals(ActionType.REST)) {
	    System.out.println("wrong action type");
	}

	if (action.getFunctionPath() != null && action.getHttpMethod() != null) {
	    System.out.println("service function not defined");
	    return null;
	}

	System.out.println("perform REST action");
	System.out.println(action.toString());

	MiniClient client = new MiniClient();
	client.setConnectorEndpoint(action.getServiceName());

	HashMap<String, String> headers = new HashMap<String, String>();
	ClientResponse response = client.sendRequest(action.getHttpMethod().toUpperCase(), action.getFunctionPath(),
		body.toJSONString(), action.getConsumes(), action.getProduces(), headers);

	System.out.println("Connect Success");
	System.out.println(response.getResponse());
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
