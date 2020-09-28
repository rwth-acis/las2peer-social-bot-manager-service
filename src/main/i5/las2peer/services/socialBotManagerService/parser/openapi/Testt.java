package i5.las2peer.services.socialBotManagerService.parser.openapi;

import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Json;
import net.minidev.json.parser.ParseException;

public class Testt {

    public static void main(String[] args) {

	String url = "http://localhost:8080/sbfmanager";

	try {
	    OpenAPIReaderV3.readModel(url);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void parse() throws ParseException {
	String specUrl = "http://localhost:8080/sbfmanager/swagger.json";

	SwaggerDeserializationResult swaggerParseResult = new SwaggerParser().readWithInfo(specUrl, null, true);
	Swagger swagger = swaggerParseResult.getSwagger();
	// System.out.println(Json.pretty(swagger));
	Path path = swagger.getPath("/training");
	Operation operation = path.getPost();

	Model schema = swagger.getDefinitions().get("Messenger");
	System.out.println(schema.getReference());

	String ref = "TelegramMessenger";
	schema = swagger.getDefinitions().get(ref);
	Json.prettyPrint(schema);
	System.out.println(schema.getClass());
	ComposedModel cm = (ComposedModel) schema;
	System.out.println(Json.pretty(cm.getProperties()));
//	System.out.println(cm.getAllOf());
//	for(Model model :cm.getAllOf()) {
//	    if (model instanceof RefModel) {
//		String ref2 = ((RefModel) model).get$ref();
//		String ref2sub = ref2.substring("#/definitions/".length());
//		if (ref.contentEquals(ref2sub) || ref.contentEquals(ref2))
//		    System.out.println(((RefModel) model).get$ref());
//
//	    }
//		System.out.println(((RefModel) model).get$ref());
	    
//	}
	

	// BodyParameter para = (BodyParameter) operation.getParameters().get(0);
	// String ref =
	// para.getSchema().getReference().substring("#/definitions/".length());
	// System.out.println(Json.pretty(swagger.getDefinitions().get(ref)));

	// System.out.println(para.getSchema().getDescription());

	//Parameter parameter = operation.getParameters().get(0);

    }



}
