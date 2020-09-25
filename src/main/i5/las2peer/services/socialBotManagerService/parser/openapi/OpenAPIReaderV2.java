package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import com.reprezen.jsonoverlay.Overlay;

import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;

public class OpenAPIReaderV2 {

    public static Swagger readModel(String jsonUrl) {

	URI modelUri = null;
	try {
	    modelUri = new URI(jsonUrl);
	} catch (URISyntaxException e1) {
	    System.out.println("no valid uri");
	    e1.printStackTrace();
	}
	Swagger model = null;
	try {
	    model = processModel(modelUri.getPath());
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return model;

    }

    public static ServiceFunction readAction(Swagger model, String functionPath, String httpMethod) {

	Operation operation = getOperationByPath(model, functionPath, httpMethod);
	ServiceFunction action = parseAction(model, operation);
	action.setActionType(ActionType.REST);
	action.setHttpMethod(httpMethod);
	action.setFunctionPath(functionPath);
	action.setFunctionName(operation.getOperationId());

	return action;

    }

    public static ServiceFunction readAction(Swagger model, String operationId) {

	Operation operation = getOperationByOperationId(model, operationId);
	ServiceFunction action = parseAction(model, operation);
	action.setActionType(ActionType.REST);
	String httpMethod = Overlay.of(operation).getPathInParent();
	action.setHttpMethod(httpMethod);
	String functionPath = Overlay.of(operation).getPathFromRoot();
	action.setFunctionPath(functionPath);
	action.setFunctionName(operation.getOperationId());

	return action;

    }

    private static Operation getOperationByPath(Swagger model, String path, String httpMethod) {

	// Path
	Path pathItem = model.getPaths().get(path);
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

    private static Operation getOperationByOperationId(Swagger openAPI, String operationId) {

	System.out.println("get Operation by Id: " + operationId);

	for (Path pathItem : openAPI.getPaths().values()) {
	    if (pathItem.getGet() != null && pathItem.getGet().getOperationId().equals(operationId))
		return pathItem.getGet();
	    if (pathItem.getPost() != null && pathItem.getPost().getOperationId().equals(operationId))
		return pathItem.getPost();
	    if (pathItem.getPut() != null && pathItem.getPut().getOperationId().equals(operationId))
		return pathItem.getPut();
	    if (pathItem.getDelete() != null && pathItem.getDelete().getOperationId().equals(operationId))
		return pathItem.getDelete();
	}

	System.out.println("Operation not found");
	return null;
    }

    private static ServiceFunction parseAction(Swagger model, Operation operation) {
	ServiceFunction action = new ServiceFunction();

	assert model != null : "no model specified";
	assert operation != null : "no operation specified";

	// Operation ID
	if (operation.getOperationId() != null) {
	    String operationId = operation.getOperationId();
	    action.setFunctionName(operationId);
	} else {
	    System.out.println("no operation id");
	}

	// Parameters
	List<Parameter> parameters = operation.getParameters();
	if (operation.getParameters() != null) {

	    for (Parameter parameter : operation.getParameters()) {

		ServiceFunctionAttribute attr = new ServiceFunctionAttribute();
		action.addAttribute(attr);

		attr.setRequired(parameter.getRequired());
		if (parameter.getName() != null)
		    attr.setName(parameter.getName());
		if (parameter.getDescription() != null)
		    attr.setDescription(parameter.getDescription());
		if (parameter.getIn() != null)
		    attr.setContentType(parameter.getIn());

		// Body Parameter
		if (parameter.getIn().contentEquals("body")) {
		    String ref = ((BodyParameter) parameter).getSchema().getReference()
			    .substring("#/definitions/".length());
		    Model schema = model.getDefinitions().get(ref);

		    attr = processAttribute(schema, attr);

		    String name = ref;
		    attr.setName(name);
		    attr = addChildrenAttributes(model, schema, attr);
		    attr.setParameterType(ParameterType.BODY);

		}
	    }

	}

	if (operation.getConsumes().contains("application/json"))
	    action.setConsumes("application/json");

	if (operation.getProduces().contains("application/json"))
	    action.setProduces("application/json");

	return action;

    }

    private static ServiceFunctionAttribute addChildrenAttributes(Swagger openAPI, Model schema,
	    ServiceFunctionAttribute bodyAttribute) {
	return addChildrenAttributes(openAPI, schema, bodyAttribute, 0);
    }

    private static ServiceFunctionAttribute addChildrenAttributes(Swagger openAPI, Model schema,
	    ServiceFunctionAttribute parentAttr, int rec) {

	Map<String, Property> properties = schema.getProperties();
	for (Map.Entry<String, Property> pair : properties.entrySet()) {

	    String name = pair.getKey();
	    Property property = pair.getValue();

	    ServiceFunctionAttribute childAttr = new ServiceFunctionAttribute();
	    childAttr.setName(name);
	    childAttr.setParameterType(ParameterType.CHILD);

	    // nested schemas
	    if (property.getType().equals("object")) {
		childAttr.setContentType("object");
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

    private static ServiceFunctionAttribute processAttribute(Property property, ServiceFunctionAttribute attr) {

	// parameter description (optional)
	if (property.getDescription() != null)
	    attr.setDescription(property.getDescription());

	// parameter example value (optional)
	if (property.getExample() != null)
	    attr.setExample(property.getExample().toString());

	// required
	attr.setRequired(property.getRequired());

	// parameter type
	System.out.println(property.getType());
	switch (property.getType()) {
	case "integer":
	    attr.setContentType("integer");
	    break;
	case "string":
	    StringProperty stringProperty = (StringProperty) property;
	    attr.setContentType("string");
	    if(stringProperty.getEnum() != null && stringProperty.getEnum().size() > 0) {
		attr.setContentType("enum");
		attr.setEnumList(stringProperty.getEnum());
	    }
	    break;
	case "boolean":
	    attr.setContentType("boolean");
	    break;
	case "array":
	    attr.setArray(true);
	    ArrayProperty arrayProperty = (ArrayProperty) property;
	    String type = arrayProperty.getItems().getType();
	    attr.setContentType(type);
	    break;
	default:
	    System.out.println("unknown parameter content type");
	}

	return attr;
    }

    private static Swagger processModel(String modelUri) throws Exception {

	SwaggerDeserializationResult swaggerParseResult = new SwaggerParser().readWithInfo(modelUri, null, true);
	Swagger swagger = swaggerParseResult.getSwagger();

	System.out.printf("== Model %s\n", modelUri);
	System.out.printf("------\n\n");
	return swagger;
    }

}
