package i5.las2peer.services.socialBotManagerService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class IntentTest {
	@Test
	public void testIntent() throws ParseException {
		String testJSONString =
				"{" +
				"    \"entities\": [" +
				"        {" +
				"            \"confidence\": 0.9281741064," +
				"            \"end\": 7," +
				"            \"entity\": \"cuisine\"," +
				"            \"extractor\": \"CRFEntityExtractor\"," +
				"            \"start\": 0," +
				"            \"value\": \"chinese\"" +
				"        }" +
				"    ]," +
				"    \"intent\": {" +
				"        \"confidence\": 0.9999506306," +
				"        \"name\": \"inform\"" +
				"    }," +
				"    \"intent_ranking\": [" +
				"        {" +
				"            \"confidence\": 0.9999506306," +
				"            \"name\": \"inform\"" +
				"        }," +
				"        {" +
				"            \"confidence\": 4.18057e-05," +
				"            \"name\": \"request_info\"" +
				"        }," +
				"        {" +
				"            \"confidence\": 7.4979e-06," +
				"            \"name\": \"thankyou\"" +
				"        }," +
				"        {" +
				"            \"confidence\": 2.85e-08," +
				"            \"name\": \"affirm\"" +
				"        }," +
				"        {" +
				"            \"confidence\": 2.09e-08," +
				"            \"name\": \"deny\"" +
				"        }," +
				"        {" +
				"            \"confidence\": 1.63e-08," +
				"            \"name\": \"greet\"" +
				"        }" +
				"    ]," +
				"    \"text\": \"chinese\"" +
				"}";
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject testJSON = (JSONObject)p.parse(testJSONString);
		assertNotNull("asdf", testJSON);
		Intent testIntent = new Intent(testJSON);
		
		assertEquals(0.9999506306f, testIntent.getConfidence(), 0.0f);
		assertEquals("inform", testIntent.getKeyword());
		
		Entity cuisineEntity = testIntent.getEntity("cuisine");
		assertNotNull(cuisineEntity);
		
		assertEquals(0.9281741064f, cuisineEntity.getConfidence(), 0.0f);
		assertEquals("chinese", cuisineEntity.getValue());
		assertEquals("cuisine", cuisineEntity.getEntityName());
	}
}
