package i5.las2peer.services.socialBotManagerService.parser.openapi;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Json;
import net.minidev.json.parser.ParseException;

public class Test {

    public static void main(String[] args) {

	String url = "https://petstore3.swagger.io/api/v3/openapi.json";

	try {
	    OpenAPIReaderV3.readModel(url);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void parse() throws ParseException {
	String specUrl = "https://petstore.swagger.io/v2/swagger.json";
	SwaggerDeserializationResult swaggerParseResult = new SwaggerParser().readWithInfo(specUrl, null, true);

	Swagger swagger = swaggerParseResult.getSwagger();
	// System.out.println(Json.pretty(swagger));
	Path path = swagger.getPath("/pet");
	// System.out.println(Json.pretty(path));
	Operation operation = path.getPost();
	Json.prettyPrint(operation.getParameters());

	BodyParameter para = (BodyParameter) operation.getParameters().get(0);
	String ref = para.getSchema().getReference().substring("#/definitions/".length());
	System.out.println(Json.pretty(swagger.getDefinitions().get(ref)));

	System.out.println(para.getSchema().getDescription());
	//Parameter parameter = operation.getParameters().get(0);

    }



}
