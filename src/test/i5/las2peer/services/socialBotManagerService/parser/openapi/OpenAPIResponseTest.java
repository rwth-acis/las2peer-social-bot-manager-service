package i5.las2peer.services.socialBotManagerService.parser.openapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gson.JsonElement;

import i5.las2peer.connectors.webConnector.client.ClientResponse;

public class OpenAPIResponseTest {
	
	ClientResponse cr;
	OpenAPIResponse response;
	JsonElement json;
	
	@Test
	public void CreateResponseTest() {
		

		cr = new ClientResponse(200);
		cr.setResponse("success");		
		response = new OpenAPIResponse(cr);
		assertEquals(200, response.getHttpCode());
		assertTrue(response.isSuccess());
		assertFalse(response.isError());
		assertEquals("success", response.getMessage());
		
		
		cr = new ClientResponse(404);
		cr.setResponse("error");		
		response = new OpenAPIResponse(cr);
		assertEquals(404, response.getHttpCode());
		assertFalse(response.isSuccess());
		assertTrue(response.isError());
		assertEquals("error", response.getMessage());
				
		
	}
	
	@Test
	public void toJSONTest() {
		
		cr = new ClientResponse(200);
		cr.setResponse("plainString");		
		response = new OpenAPIResponse(cr);
		json = response.getAsJSON();
		assertNotNull(json);
		System.out.println(json);
		assertTrue(json.isJsonPrimitive());
		
		cr = new ClientResponse(200);
		cr.setResponse("{\"note\":{\"from\":\"world\",\"to\":\"hello\",\"body\":\"test\"}}");		
		response = new OpenAPIResponse(cr);
		json = response.getAsJSON();
		assertNotNull(json);
		assertEquals("{\"note\":{\"from\":\"world\",\"to\":\"hello\",\"body\":\"test\"}}", json.toString());
		assertTrue(json.isJsonObject());
		
		cr = new ClientResponse(200);
		cr.setResponse("[1,2,3,4,5]");		
		response = new OpenAPIResponse(cr);
		json = response.getAsJSON();
		assertNotNull(json);
		assertEquals("[1,2,3,4,5]", json.toString());
		assertTrue(json.isJsonArray());
		
		cr = new ClientResponse(200);
		cr.setResponse("<note>\n" + 
				"<to>hello</to>\n" + 
				"<from>world</from>\n" +
				"<body>test</body>\n" + 
				"</note>");		
		response = new OpenAPIResponse(cr);
		json = response.getAsJSON();
		assertNotNull(json);
		assertEquals("{\"note\":{\"from\":\"world\",\"to\":\"hello\",\"body\":\"test\"}}", json.toString());
		assertTrue(json.isJsonObject());
			
		cr = new ClientResponse(200);
		cr.setResponse(null);		
		response = new OpenAPIResponse(cr);
		json = response.getAsJSON();
		assertNotNull(json);
		assertTrue(json.isJsonNull());
		
	}
}
