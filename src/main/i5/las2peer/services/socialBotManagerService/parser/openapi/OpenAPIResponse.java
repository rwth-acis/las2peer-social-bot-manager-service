package i5.las2peer.services.socialBotManagerService.parser.openapi;

import org.json.JSONObject;
import org.json.XML;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import i5.las2peer.connectors.webConnector.client.ClientResponse;

/**
 * Response of an OpenAPIAction
 *
 */
public class OpenAPIResponse {

	ClientResponse response;

	public OpenAPIResponse(ClientResponse response) {
		assert response != null : "OpenAPIResponse has no clientResponse";
		this.response = response;
		System.out.println("Create new OpenAPIResponse");
		System.out.println("http: " + response.getHttpCode() + " response: " + response.getResponse());
	}

	public int getHttpCode() {
		assert response != null;
		return this.response.getHttpCode();
	}

	public String getMessage() {
		assert response != null;
		return this.response.getResponse();
	}

	public boolean isSuccess() {
		int httpCode = getHttpCode();
		if (httpCode >= 200 && httpCode < 300)
			return true;
		return false;
	}

	public boolean isError() {
		int httpCode = getHttpCode();
		if (httpCode >= 300 || httpCode < 200)
			return true;
		return false;
	}

	public boolean isJSON() {
		return this.getAsJSON() != null;
	}

	public JsonElement getAsJSON() {
		System.out.println("get as JSON " + this.getMessage());
		
		String response = this.getMessage();
		if(response == null)
			response = "";

		// convert direct to JSON object
		try {
			JsonElement res = JsonParser.parseString(response);
			return res;
		} catch (Exception e) {
			e.fillInStackTrace();
		}

		// convert from XML to JSON object
		try {
			JSONObject json = XML.toJSONObject(response);
			JsonElement res = JsonParser.parseString(json.toString());
			return res;
		} catch (Exception e) {
			e.fillInStackTrace();
		}
		
		return null;
	}

}
