package i5.las2peer.services.socialBotManagerService.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.reprezen.jsonoverlay.Overlay;
import com.reprezen.kaizen.oasparser.OpenApi;
import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.MediaType;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.model3.Operation;
import com.reprezen.kaizen.oasparser.model3.Parameter;
import com.reprezen.kaizen.oasparser.model3.Path;
import com.reprezen.kaizen.oasparser.model3.Schema;
import com.reprezen.kaizen.oasparser.val.ValidationResults.ValidationItem;

import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;

public class OpenAPIReader {

	public static OpenApi3 readModel(String url) {

		boolean validate = true;
		URI modelUri = null;
		try {
			modelUri = new URI(url);
		} catch (URISyntaxException e1) {
			System.out.println("no valid uri");
			e1.printStackTrace();
		}
		OpenApi3 model = null;
		try {
			model = processModel(modelUri, validate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;

	}

	public static ServiceFunction readAction(String baseUrl, String functionPath, String httpMethod) {

		String swaggerUrl = "https://petstore3.swagger.io/api/v3/openapi.json";
		OpenApi3 model = readModel(swaggerUrl);

		Operation operation = getOperationByPath(model, functionPath, httpMethod);
		ServiceFunction action = parseAction(model, operation);
		action.setActionType(ActionType.REST);
		action.setHttpMethod(httpMethod);
		action.setFunctionPath(baseUrl + functionPath);
		action.setFunctionName(operation.getOperationId());

		return action;

	}

	private static Operation getOperationByPath(OpenApi3 openAPI, String path, String httpMethod) {

		// Path
		Path pathItem = openAPI.getPaths().get(path);
		if (pathItem == null)
			System.out.println("invalid path: " + path);

		// HTTP method
		Operation operation = null;
		switch (httpMethod) {
		case "get":
			operation = pathItem.getGet();
			break;
		case "post":
			operation = pathItem.getPost();
			break;
		case "put":
			operation = pathItem.getPut();
			break;
		case "delete":
			operation = pathItem.getDelete();
			break;
		default:
			System.out.println("invalid http method: " + httpMethod);
		}

		if (operation == null)
			System.out.println("no operation found");
		return operation;

	}

	private static ServiceFunction parseAction(OpenApi openAPI, Operation operation) {
		ServiceFunction action = new ServiceFunction();

		// Operation ID
		String operationId = operation.getOperationId();
		if (operationId != null) {
			action.setFunctionName(operationId);
		} else {
			System.out.println("no operation id");
		}

		// Path Parameters
		List<Parameter> parameters = operation.getParameters();
		if (operation.getParameters() != null) {

			for (Parameter parameter : operation.getParameters()) {

				ServiceFunctionAttribute attr = new ServiceFunctionAttribute();
				if (parameter.getName() != null)
					attr.setName(parameter.getName());
				if (parameter.getDescription() != null)
					attr.setDescription(parameter.getDescription());
				if (parameter.getRequired() != null)
					attr.setRequired(parameter.getRequired());
				if (parameter.getIn() != null)
					attr.setContentType(parameter.getIn());

				if (parameter.getSchema() != null) {
					attr = processAttribute(parameter.getSchema(), attr);
				}
				action.addAttribute(attr);
			}
		}

		// Body
		if (operation.getRequestBody() != null) {

			// Content MediaType
			String contentType = "application/json";
			MediaType mediaType = operation.getRequestBody().getContentMediaType(contentType);
			if (mediaType == null) {
				System.out.println("invalid media type: " + contentType);
			} else {
				action.setConsumes(contentType);
			}

			// ContentBody
			ServiceFunctionAttribute bodyAttribute = new ServiceFunctionAttribute();
			if (mediaType.getSchema() != null) {
				Schema schema = mediaType.getSchema();
				String name = schema.getXml().getName();
				bodyAttribute.setName(name);
				bodyAttribute = addChildrenAttributes(openAPI, schema, bodyAttribute);
				bodyAttribute.setContentType("body");
				action.addAttribute(bodyAttribute);
			}

		}

		if (operation.getResponses().get("200").getContentMediaType("application/json") != null)
			action.setProduces("application/json");

		return action;

	}

	private static ServiceFunctionAttribute addChildrenAttributes(OpenApi openAPI, Schema schema,
			ServiceFunctionAttribute bodyAttribute) {
		return addChildrenAttributes(openAPI, schema, bodyAttribute, 0);
	}

	private static ServiceFunctionAttribute addChildrenAttributes(OpenApi openAPI, Schema schema,
			ServiceFunctionAttribute parentAttr, int rec) {

		Map<String, Schema> properties = schema.getProperties();
		for (Map.Entry<String, Schema> pair : properties.entrySet()) {

			String name = pair.getKey();
			Schema property = pair.getValue();

			ServiceFunctionAttribute childAttr = new ServiceFunctionAttribute();
			childAttr.setName(name);
			childAttr.setContentType("child");

			// nested schemas
			if (property.getType().equals("object")) {
				if (rec > 8) {
					System.out.println("to much nesting");
				} else {
					childAttr = addChildrenAttributes(openAPI, property, childAttr, rec + 1);
				}
				// values
			} else {
				childAttr = processAttribute(property, childAttr);
			}

			parentAttr.addChildAttribute(childAttr);
			childAttr.setParent(parentAttr);

		}
		return parentAttr;

	}

	private static ServiceFunctionAttribute processAttribute(Schema property, ServiceFunctionAttribute attr) {

		// parameter description (optional)
		if (property.getDescription() != null)
			attr.setDescription(property.getDescription());

		// parameter example value (optional)
		if (property.getExample() != null)
			attr.setExample(property.getExample().toString());

		// enum parameter
		if (property.hasEnums()) {
			attr.setContentType("enum");
			attr.setEnumList(property.getEnums());
			return attr;
		}

		// parameter type
		switch (property.getType()) {
		case "object":
			break;
		case "integer":
			attr.setContentType("integer");
			break;
		case "string":
			attr.setContentType("string");
			break;
		case "boolean":
			attr.setContentType("boolean");
			break;
		case "array":
			attr.setContentType("array");
			break;
		default:
			System.out.println("unknown parameter content type");
		}

		return attr;
	}

	private static OpenApi3 processModel(URI modelUri, boolean validate) throws Exception {
		OpenApi3 model = new OpenApi3Parser().parse(modelUri, validate);
		System.out.printf("== Model %s\n", modelUri);
		if (!validate || model.isValid()) {
			// describeModel(model);
		} else {
			for (ValidationItem item : model.getValidationItems()) {
				System.out.println(item);
			}
		}
		System.out.printf("------\n\n");
		return model;
	}

	private static void describeModel(OpenApi3 model) {
		System.out.printf("Title: %s\n", model.getInfo().getTitle());
		System.out.println("Schemas " + model.getSchema("Pet"));
		System.out.println(model.getPath("/pet").getPost().getRequestBody().getContentMediaType("application/json")
				.getSchema().getProperty("name"));
		System.out.println(model.getPath("/pet").getPost().getRequestBody().getContentMediaType("application/json")
				.getSchema().getProperty("category"));
		System.out.println(model.getPath("/pet").getPost().getRequestBody().getContentMediaType("application/json")
				.getSchema().getProperty("status"));

		for (Path path : model.getPaths().values()) {
			System.out.printf("Path %s:\n", Overlay.of(path).getPathInParent());
			for (Operation op : path.getOperations().values()) {
				System.out.printf("  %s: [%s] %s\n", Overlay.of(op).getPathInParent().toUpperCase(),
						op.getOperationId(), op.getSummary());
				for (Parameter param : op.getParameters()) {
					System.out.printf("    %s[%s]:, %s - %s\n", param.getName(), param.getIn(), getParameterType(param),
							param.getDescription());
				}
			}
		}
	}

	private static String getParameterType(Parameter param) {
		Schema schema = param.getSchema();
		return schema != null ? schema.getType() : "unknown";
	}

}
