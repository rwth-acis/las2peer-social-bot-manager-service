package i5.las2peer.services.socialBotManagerService.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.FrameMapper;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIConnector;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;

public class VisualizerTest {

	@Test
	public void VisualizeTest() {

		String content = "";

		try {
			content = new String(Files.readAllBytes(Paths.get("testData/testSwaggerAPI.json")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SwaggerDeserializationResult swaggerParseResult = new SwaggerParser().readWithInfo(content);
		Swagger swagger = swaggerParseResult.getSwagger();

		ServiceFunction action = OpenAPIConnector.readFunction(swagger, "postBasicScheme");
		action.setBasePath("test");
		FrameMapper mapper = new FrameMapper();
		Frame frame = null;
		try {
			frame = mapper.createAndMap(action);
		} catch (ParseBotException e) {
			e.printStackTrace();
		}

		Visualizer vis = new Visualizer();
		//vis.draw(frame);

	}

}
