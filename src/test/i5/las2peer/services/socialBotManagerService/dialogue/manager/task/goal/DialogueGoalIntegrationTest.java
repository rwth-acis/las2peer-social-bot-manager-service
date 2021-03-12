package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import i5.las2peer.services.socialBotManagerService.parser.openapi.FrameMapper;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import net.minidev.json.JSONObject;

/**
 * Test the process from reading a swagger documentation to generating a
 * suitable OpenAPIAction from a DialogeGoal
 * 
 * Reads Swagger documentation file, Parses into Frame, Parses into DialogeGoal
 * 
 */
public class DialogueGoalIntegrationTest {

	static Swagger swagger;

	@BeforeClass
	public static void setUp() {

		String content = "";

		try {
			content = new String(Files.readAllBytes(Paths.get("testData/testSwagger.json")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		SwaggerDeserializationResult swaggerParseResult = new SwaggerParser().readWithInfo(content);
		swagger = swaggerParseResult.getSwagger();

	}

	public Frame read(String operationId) {

		ServiceFunction action = OpenAPIConnector.readFunction(swagger, operationId);
		action.setBasePath("test");
		FrameMapper mapper = new FrameMapper();
		Frame frame = null;
		try {
			frame = mapper.createAndMap(action);
		} catch (ParseBotException e) {
			e.printStackTrace();
		}

		return frame;

	}

	@Test
	public void queryParamTest() {

		Frame frame = read("getWithQuery");
		assertNotNull(frame);
		assertNotNull(frame.getSlots());

		DialogueGoal goal = new DialogueGoal(frame);
		assertNotNull(goal);

		goal.fill("stringParameter", "test");
		goal.fill("numberParameter", "123");

		OpenAPIAction action = goal.getOpenAPIAction();
		assertNotNull(action.getQueryParameters());
		assertEquals(2, action.getQueryParameters().size());
		assertTrue(action.getQueryParameters().containsKey("stringParameter"));
		assertTrue(action.getQueryParameters().containsKey("numberParameter"));

	}

	@Test
	public void pathParamTest() {

		Frame frame = read("getWithPath");
		assertNotNull(frame);
		assertNotNull(frame.getSlots());

		DialogueGoal goal = new DialogueGoal(frame);
		assertNotNull(goal);

		goal.fill("pathParameter", "test");

		OpenAPIAction action = goal.getOpenAPIAction();
		assertNotNull(action.getPathParameters());
		assertEquals(1, action.getPathParameters().size());
		assertTrue(action.getPathParameters().containsKey("pathParameter"));

	}

	@Test
	public void BodyParamTest() {

		Frame frame = read("postBasicScheme");
		assertNotNull(frame);
		assertNotNull(frame.getSlots());

		DialogueGoal goal = new DialogueGoal(frame);
		assertNotNull(goal);

		goal.fill("stringParameter", "test");
		goal.fill("numberParameter", "123");
		goal.fill("enumParameter", "A");
		goal.fill("arrayParameter", "test1");
		goal.fill("arrayParameter", "test2");
		goal.fill("arrayParameter", "test3");

		JSONObject bodyJson = goal.toBodyJSON();
		assertNotNull(bodyJson);
		assertEquals(
				"{\"enumParameter\":\"A\",\"arrayParameter\":[\"test1\",\"test2\",\"test3\"],\"stringParameter\":\"test\",\"numberParameter\":\"123\"}",
				bodyJson.toJSONString());

		OpenAPIAction action = goal.getOpenAPIAction();
		assertNotNull(action.getBodyParameter());
		assertEquals(
				"{\"enumParameter\":\"A\",\"arrayParameter\":[\"test1\",\"test2\",\"test3\"],\"stringParameter\":\"test\",\"numberParameter\":\"123\"}",
				action.getBodyParameter().toJSONString());

	}

	@Test
	public void CombinedParameterTest() {

		Frame frame = read("getWithCombined");
		assertNotNull(frame);
		assertNotNull(frame.getSlots());

		DialogueGoal goal = new DialogueGoal(frame);
		assertNotNull(goal);

		goal.fill("pathParameter", "test");
		goal.fill("queryParameter", "test");
		goal.fill("stringParameter", "test");
		goal.fill("numberParameter", "123");
		goal.fill("enumParameter", "A");

		OpenAPIAction action = goal.getOpenAPIAction();
		assertNotNull(action.getPathParameters());
		assertEquals(1, action.getPathParameters().size());
		assertTrue(action.getPathParameters().containsKey("pathParameter"));
		assertNotNull(action.getQueryParameters());
		assertEquals(1, action.getQueryParameters().size());
		assertTrue(action.getQueryParameters().containsKey("queryParameter"));
		assertNotNull(action.getBodyParameter());
		assertEquals("{\"enumParameter\":\"A\",\"stringParameter\":\"test\",\"numberParameter\":\"123\"}",
				action.getBodyParameter().toJSONString());

	}

	@Test
	public void BasicCompositionTest() {

		Frame frame = read("postComposition");
		assertNotNull(frame);
		assertNotNull(frame.getSlots());

		DialogueGoal goal = new DialogueGoal(frame);
		assertNotNull(goal);

		goal.fill("stringParameter", "example");
		goal.fill("subParameterOne", "one");
		goal.fill("subParameterTwo", "two");

		JSONObject bodyJson = goal.toBodyJSON();
		assertNotNull(bodyJson);
		assertEquals(2, bodyJson.size());
		assertTrue(bodyJson.containsKey("composition"));
		assertTrue(bodyJson.containsKey("stringParameter"));
		assertEquals("example", bodyJson.getAsString("stringParameter"));

		JSONObject composition = ((JSONObject) bodyJson.get("composition"));
		assertEquals(2, composition.size());
		assertTrue(composition.containsKey("subParameterOne"));
		assertTrue(composition.containsKey("subParameterTwo"));
		assertEquals("one", composition.getAsString("subParameterOne"));
		assertEquals("two", composition.getAsString("subParameterTwo"));

	}

	@Test
	public void ArrayCompositionTest() {

		Frame frame = read("postArrayScheme");
		assertNotNull(frame);
		assertNotNull(frame.getSlots());

		DialogueGoal goal = new DialogueGoal(frame);
		assertNotNull(goal);

		goal.fill("subParameterOne", "one");
		goal.fill("subParameterTwo", "two");

		JSONObject bodyJson = goal.toBodyJSON();
		assertNotNull(bodyJson);
		assertEquals(1, bodyJson.size());
		assertTrue(bodyJson.containsKey("composition"));

		assertEquals("{\"composition\":[{\"subParameterOne\":\"one\",\"subParameterTwo\":\"two\"}]}",
				bodyJson.toJSONString());

	}

	@Test
	public void PolymorphismTest() {

		Frame frame = read("postPolymorph");
		assertNotNull(frame);
		assertNotNull(frame.getSlots());

		DialogueGoal goal = new DialogueGoal(frame);
		assertNotNull(goal);
		goal.fill("type", "Child A");
		goal.fill("subParameterOne", "one");

		JSONObject bodyJson = goal.toBodyJSON();
		assertNotNull(bodyJson);
		assertEquals(2, bodyJson.size());
		assertTrue(bodyJson.containsKey("type"));
		assertTrue(bodyJson.containsKey("subParameterOne"));
		assertEquals("Child A", bodyJson.getAsString("type"));
		assertEquals("one", bodyJson.getAsString("subParameterOne"));

		goal = new DialogueGoal(frame);
		assertNotNull(goal);
		goal.fill("type", "Child B");
		goal.fill("subParameterTwo", "two");

		bodyJson = goal.toBodyJSON();
		assertNotNull(bodyJson);
		assertEquals(2, bodyJson.size());
		assertTrue(bodyJson.containsKey("type"));
		assertTrue(bodyJson.containsKey("subParameterTwo"));
		assertEquals("Child B", bodyJson.getAsString("type"));
		assertEquals("two", bodyJson.getAsString("subParameterTwo"));

	}

	@Test
	public void SelfCompositionPolymorphTest() {

		Frame frame = read("postSelfComposition");
		assertNotNull(frame);
		assertNotNull(frame.getSlots());

		DialogueGoal goal = new DialogueGoal(frame);
		assertNotNull(goal);
		goal.fill("type", "CCA");
		goal.fill("subParameterOne", "one");

		JSONObject bodyJson = goal.toBodyJSON();
		assertNotNull(bodyJson);
		assertEquals(2, bodyJson.size());
		assertTrue(bodyJson.containsKey("type"));
		assertTrue(bodyJson.containsKey("subParameterOne"));
		assertEquals("CCA", bodyJson.getAsString("type"));
		assertEquals("one", bodyJson.getAsString("subParameterOne"));

		goal = new DialogueGoal(frame);
		assertNotNull(goal);
		goal.fill("type", "CCB");
		goal.fill("subParameterTwo", "two");
		goal.fill("SelfComposition_type_CCB_composition_type", "CCA");
		goal.fill("subParameterOne", "one");

		bodyJson = goal.toBodyJSON();
		assertNotNull(bodyJson);
		assertTrue(bodyJson.containsKey("type"));
		assertTrue(bodyJson.containsKey("subParameterTwo"));
		assertTrue(bodyJson.containsKey("composition"));

		assertEquals(bodyJson.toJSONString(),
				"{\"subParameterTwo\":\"two\",\"type\":\"CCB\",\"composition\":[{\"type\":\"CCA\",\"subParameterOne\":\"one\"}]}");

	}

}
