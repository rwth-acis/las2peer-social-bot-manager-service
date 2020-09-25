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
import net.minidev.json.JSONObject;

public class OpenAPIConnector {

    public static ServiceFunction readFunction(ServiceFunction action) {

	if (action.getServiceName() == null) {
	    System.out.println("no base url specified");
	    return null;
	}

	// read model
	String modelUrl = getSwaggerDocument(action.getServiceName());
	if (modelUrl == null) {
	    System.out.println("swagger definition not found");
	    return null;
	}
	OpenApi3 model = OpenAPIReaderV3.readModel(modelUrl);
	if (model == null) {
	    System.out.println("open api model not found: " + modelUrl);
	    return null;
	}

	// read function
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
	    url = new URL(baseUrl + "/swagger.json");

	    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
	    huc.setRequestMethod("HEAD");
	    int responseCode = huc.getResponseCode();

	    if (responseCode == 200)
		return url.getPath();

	    url = new URL(baseUrl + "/api/v3/openapi.json");
	    huc = (HttpURLConnection) url.openConnection();
	    huc.setRequestMethod("HEAD");
	    responseCode = huc.getResponseCode();

	    if (responseCode == 200)
		return url.getPath();

	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (IOException e) {

	}

	return null;

    }

}
