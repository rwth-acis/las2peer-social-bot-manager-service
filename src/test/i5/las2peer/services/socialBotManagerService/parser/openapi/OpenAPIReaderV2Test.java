package i5.las2peer.services.socialBotManagerService.parser.openapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;

public class OpenAPIReaderV2Test {

	static Swagger swagger;

	/**
	 * Read the swagger documentation
	 */
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

	/**
	 * Test if parsed ServiceFunction includes Response information
	 */
	@Test
	public void readResponseTest() {

		ServiceFunction function = OpenAPIConnector.readFunction(swagger, "getWithPath");
		assertNotNull(function.getProduces());
		assertNotNull(function.getResponse());
		ServiceFunctionAttribute bodyAttr = function.getResponse();
		assertEquals(4, bodyAttr.getChildAttributes().size());

		List<String> parameterNames = new ArrayList<>();
		parameterNames.add("stringParameter");
		parameterNames.add("numberParameter");
		parameterNames.add("enumParameter");
		parameterNames.add("arrayParameter");
		for (String parameterName : parameterNames) {
			boolean flag = false;
			for (ServiceFunctionAttribute attr : bodyAttr.getChildAttributes()) {
				if (attr.getName().contentEquals(parameterName))
					flag = true;
			}
			assertTrue(flag);
		}

	}

	/**
	 * Test if reader can read PetStore swagger v2 documentation.
	 * 
	 */
	@Test
	public void frameMapPetstoreV2Test() throws ParseBotException {

		FrameMapper mapper = new FrameMapper();
		ServiceFunction function = new ServiceFunction();
		function.setHttpMethod("post");
		function.setServiceName("https://petstore.swagger.io/");
		function.setFunctionName("addPet");
		Frame frame = mapper.create(function);

		assertNotNull(frame.getDescendants());
		assertNotNull(frame.getSlots());
		assertEquals(1, frame.getSlots().size());
		assertNotNull(frame.getSlot("Pet"));
		assertEquals(11, frame.getDescendants().size());
		assertNotNull(frame.getSlot("Pet_name"));

		assertNotNull(frame.getCommand());

	}

}
